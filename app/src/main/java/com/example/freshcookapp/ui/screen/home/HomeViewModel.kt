package com.example.freshcookapp.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.data.mapper.toRecipe
import com.example.freshcookapp.domain.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.freshcookapp.data.local.dao.CategoryDao
import com.example.freshcookapp.data.repository.CategoryRepository
import com.example.freshcookapp.data.repository.SearchRepository
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Collections
import java.util.HashSet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.debounce

data class SuggestItem(
    val keyword: String,
    val timestamp: Long,
    val imageUrl: String?
)

class HomeViewModel(
    private val recipeRepo: RecipeRepository,
    private val categoryRepo: CategoryRepository,
    private val searchRepo: SearchRepository
) : ViewModel() {

    private var unreadListener: ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance()

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications

    // ======= CATEGORIES =======
    val categories = categoryRepo.getLocalCategories()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // ======= TRENDING =======
    private val _trendingRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val trendingRecipes: StateFlow<List<Recipe>> = _trendingRecipes.asStateFlow()

    // ======= NEW DISHES =======
    private val _newDishes = MutableStateFlow<List<Recipe>>(emptyList())
    val newDishes: StateFlow<List<Recipe>> = _newDishes.asStateFlow()

    private val inFlightFavorites: MutableSet<String> = Collections.synchronizedSet(HashSet())
    private val _inFlightIds = MutableStateFlow<Set<String>>(emptySet())
    val inFlightIds = _inFlightIds.asStateFlow()

    // ======= SUGGESTIONS =======
    val suggestedSearch = searchRepo.getAllHistory()
        .flatMapLatest { historyList ->
            flow {
                val finalList = historyList.map { historyItem ->
                    val matchedRecipes = recipeRepo.searchRecipesByName(historyItem.query)
                    val randomImage = matchedRecipes.randomOrNull()?.imageUrl
                    SuggestItem(
                        keyword = historyItem.query,
                        timestamp = historyItem.timestamp,
                        imageUrl = randomImage
                    )
                }
                emit(finalList)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Notification Listener
    fun startNotificationListener() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        unreadListener?.remove()
        unreadListener = firestore.collection("users")
            .document(uid)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    _hasUnreadNotifications.value = snapshot.size() > 0
                }
            }
    }

    // ======= FAVORITE =======
    fun toggleFavorite(recipeId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val started = inFlightFavorites.add(recipeId)
        if (!started) return
        _inFlightIds.value = inFlightFavorites.toSet()

        viewModelScope.launch {
            try {
                val trendingList = _trendingRecipes.value
                val newDishesList = _newDishes.value
                val found = (trendingList + newDishesList).firstOrNull { it.id == recipeId }

                if (found == null) {
                    inFlightFavorites.remove(recipeId)
                    _inFlightIds.value = inFlightFavorites.toSet()
                    return@launch
                }
                val desiredState = !found.isFavorite
                recipeRepo.toggleFavoriteWithRemote(user.uid, recipeId, desiredState)
                inFlightFavorites.remove(recipeId)
                _inFlightIds.value = inFlightFavorites.toSet()

            } catch (e: Exception) {
                Log.e("HomeVM", "toggleFavorite EXCEPTION: $recipeId", e)
                inFlightFavorites.remove(recipeId)
                _inFlightIds.value = inFlightFavorites.toSet()
            }
        }
    }

    // ======= USER INFO =======
    private val _userName = MutableStateFlow<String?>("User")
    val userName = _userName.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl = _userPhotoUrl.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        viewModelScope.launch { categoryRepo.syncCategories() }

        viewModelScope.launch {
            recipeRepo.getTrendingRecipes()
                .map { list -> list.map { it.toRecipe() } }
                .collect { _trendingRecipes.value = it }
        }

        viewModelScope.launch {
            recipeRepo.getNewDishes()
                .map { list -> list.map { it.toRecipe() } }
                .collect { _newDishes.value = it }
        }

        viewModelScope.launch {
            recipeRepo.getFavoriteRecipes()
                .map { list -> list.map { it.id }.toSet() }
                .debounce(150)
                .distinctUntilChanged()
                .collect { _favoriteIds.value = it }
        }
        loadCurrentUser()
    }

    // --- SỬA CHỖ NÀY: Bỏ đoạn thêm timestamp vào URL ---
    private fun loadCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _userName.value = document.getString("fullName") ?: "User"
                        // Chỉ lấy URL gốc, không thêm ?t=... nữa để tận dụng Cache
                        _userPhotoUrl.value = document.getString("photoUrl")
                    } else {
                        _userName.value = "User"
                        _userPhotoUrl.value = null
                    }
                }
                .addOnFailureListener {
                    _userName.value = "User"
                    _userPhotoUrl.value = null
                }
        } else {
            _userName.value = "User"
            _userPhotoUrl.value = null
        }
    }

    fun refreshUserData() {
        loadCurrentUser()
    }

    companion object {
        class Factory(
            private val recipeRepo: RecipeRepository,
            private val categoryDao: CategoryDao,
            private val db: AppDatabase
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val categoryRepository = CategoryRepository(categoryDao)
                val searchRepo = SearchRepository(
                    recipeDao = db.recipeDao(),
                    historyDao = db.searchHistoryDao()
                )
                return HomeViewModel(
                    recipeRepo,
                    categoryRepository,
                    searchRepo
                ) as T
            }
        }
    }
}