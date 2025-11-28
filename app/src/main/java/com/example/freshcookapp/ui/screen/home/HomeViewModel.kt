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
import kotlinx.coroutines.flow.map // <-- Quan trọng: import map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.HashSet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.debounce

data class SuggestItem(
    val keyword: String,
    val timestamp: Long,
    val imageUrl: String?   // random image
)

class HomeViewModel(
    private val recipeRepo: RecipeRepository,
    private val categoryRepo: CategoryRepository,
    private val searchRepo: SearchRepository   // 👈 THÊM
) : ViewModel() {

    private var unreadListener: ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance() // Thêm một thể hiện của Firestore

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

    // In-flight toggles to avoid concurrent operations on same recipe
    private val inFlightFavorites: MutableSet<String> = Collections.synchronizedSet(HashSet())
    private val _inFlightIds = MutableStateFlow<Set<String>>(emptySet())
    val inFlightIds = _inFlightIds.asStateFlow()

    // ======= GỢI Ý THEO TỪ KHÓA (KHÔNG HIỂN THỊ TOÀN BỘ RECIPE) =======
    val suggestedSearch = searchRepo.getAllHistory()   // Flow<List<SearchHistoryEntity>>
        .flatMapLatest { historyList ->
            flow {
                val finalList = historyList.map { historyItem ->

                    // Lấy danh sách món ăn theo keyword
                    val matchedRecipes = recipeRepo.searchByNameLocal(historyItem.query)

                    // Lấy ảnh ngẫu nhiên
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


    // nòication
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


    // ======= LIKE (FAVORITE) =======
    fun toggleFavorite(recipeId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // Prevent concurrent toggles on same recipe
        val started = inFlightFavorites.add(recipeId)
        if (!started) return
        _inFlightIds.value = inFlightFavorites.toSet()

        viewModelScope.launch {
            try {
                Log.d("HomeVM", "toggleFavorite START (delegated): $recipeId, inFlight=${inFlightFavorites.size}")

                // Read a local copy only to decide desired state (don't apply optimistic update here;
                // repository will handle optimistic local update and rollback atomically).
                val trendingList = _trendingRecipes.value
                val newDishesList = _newDishes.value

                val found = (trendingList + newDishesList).firstOrNull { it.id == recipeId }
                if (found == null) {
                    Log.w("HomeVM", "toggleFavorite: recipe not found locally: $recipeId")
                    inFlightFavorites.remove(recipeId)
                    _inFlightIds.value = inFlightFavorites.toSet()
                    return@launch
                }

                val desiredState = !found.isFavorite

                // Delegate the full atomic optimistic toggle + remote transaction to repository
                recipeRepo.toggleFavoriteWithRemote(user.uid, recipeId, desiredState)

                // repository will persist final state in Room; we simply clear in-flight below
                inFlightFavorites.remove(recipeId)
                _inFlightIds.value = inFlightFavorites.toSet()

            } catch (e: Exception) {
                Log.e("HomeVM", "toggleFavorite EXCEPTION: $recipeId", e)
                // Ensure cleanup on unexpected errors
                inFlightFavorites.remove(recipeId)
                _inFlightIds.value = inFlightFavorites.toSet()
            }
        }
    }

    // ======= USER INFO =======
    private val _userName = MutableStateFlow<String?>("User") // Giá trị mặc định
    val userName = _userName.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl = _userPhotoUrl.asStateFlow()

    // ======= NEW: FAVORITES IDS SET =======
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        // Sync category
        viewModelScope.launch {
            categoryRepo.syncCategories()
        }

        // Collect trending recipes
        viewModelScope.launch {
            recipeRepo.getTrendingRecipes()
                .map { list -> list.map { it.toRecipe() } }
                .collect { _trendingRecipes.value = it }
        }

        // Collect new dishes
        viewModelScope.launch {
            recipeRepo.getNewDishes()
                .map { list -> list.map { it.toRecipe() } }
                .collect { _newDishes.value = it }
        }

        // Collect favorite ids (single source of truth for isFavorite in UI)
        viewModelScope.launch {
            recipeRepo.getFavoriteRecipes()
                .map { list -> list.map { it.id }.toSet() }
                // Debounce a bit to coalesce quick transient changes (reduces UI flicker)
                .debounce(150)
                .distinctUntilChanged()
                .collect {
                    Log.d("HomeVM", "favoriteIds EMIT (debounced): ${it.size} -> ${it.joinToString(",")} ")
                    _favoriteIds.value = it
                }
        }

        loadCurrentUser()
    }

    // SỬA LẠI HÀM NÀY
    private fun loadCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Nếu có người dùng, lấy UID và truy vấn Firestore
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Lấy fullName từ Firestore
                        _userName.value = document.getString("fullName") ?: "User"
                        _userPhotoUrl.value = document.getString("photoUrl")
                    } else {
                        // Trường hợp có user auth nhưng không có document trong firestore
                        _userName.value = "User" 
                    }
                }
                .addOnFailureListener {
                    // Xử lý lỗi nếu có
                    _userName.value = "User"
                }
        } else {
            // Không có người dùng nào đăng nhập
            _userName.value = "User"
        }
    }

    // ======= FACTORY =======
    companion object {
        class Factory(
            private val recipeRepo: RecipeRepository,
            private val categoryDao: CategoryDao,
            private val db: AppDatabase             // 👈 TRUYỀN DB
        ) : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val categoryRepository = CategoryRepository(categoryDao)

                // 👇 SearchRepository cần RecipeDao + SearchHistoryDao
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
