package com.example.freshcookapp.ui.screen.home

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
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map // <-- Quan trọng: import map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

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
    val trendingRecipes = recipeRepo.getTrendingRecipes()
        .map { list -> list.map { it.toRecipe() } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // ======= NEW DISHES =======
    val newDishes = recipeRepo.getNewDishes()
        .map { list -> list.map { it.toRecipe() } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

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

        viewModelScope.launch {

            // Lấy trending current list
            val list = trendingRecipes.value.toMutableList()
            val index = list.indexOfFirst { it.id == recipeId }
            if (index == -1) return@launch

            val recipe = list[index]
            val newStatus = !recipe.isFavorite
            val newCount = if (newStatus) recipe.likeCount + 1 else recipe.likeCount - 1

            // ⭐ UI update ngay
            list[index] = recipe.copy(isFavorite = newStatus, likeCount = newCount)
            recipeRepo.updateFavoriteLocal(recipeId, newStatus, newCount)

            val recipeRef = firestore.collection("recipes").document(recipeId)
            val userFavRef = firestore.collection("users")
                .document(user.uid)
                .collection("favorites")
                .document(recipeId)

            // ⭐ Firestore transaction
            firestore.runTransaction { tx ->
                val snap = tx.get(userFavRef)

                if (snap.exists()) {
                    tx.delete(userFavRef)
                    tx.update(
                        recipeRef,
                        "likeCount",
                        FieldValue.increment(-1)
                    )
                    false
                } else {
                    tx.set(
                        userFavRef,
                        mapOf(
                            "addedAt" to FieldValue.serverTimestamp(),
                            "recipeId" to recipeId
                        )
                    )
                    tx.update(
                        recipeRef,
                        "likeCount",
                        FieldValue.increment(1)
                    )
                    true
                }
            }.addOnSuccessListener { isLiked ->
                viewModelScope.launch {
                    recipeRepo.toggleFavorite(recipeId, isLiked, newCount)
                }
            }
        }
    }

    // ======= USER INFO =======
    private val _userName = MutableStateFlow<String?>("User") // Giá trị mặc định
    val userName = _userName.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl = _userPhotoUrl.asStateFlow()

    init {
        // Sync category
        viewModelScope.launch {
            categoryRepo.syncCategories()
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
