package com.example.freshcookapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.data.mapper.toRecipe
import com.example.freshcookapp.domain.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.freshcookapp.data.local.dao.CategoryDao
import com.example.freshcookapp.data.local.entity.CategoryEntity
import com.example.freshcookapp.data.local.entity.NewDishEntity
import com.example.freshcookapp.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map // <-- Quan trọng: import map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val recipeRepo: RecipeRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    // ==== CATEGORIES ====
    val categories = categoryRepo.getLocalCategories()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // ==== RECOMMENDED RECIPES ====
//    val recommendedRecipes = recipeRepo.getRecommendedRecipes()
//        .map { list -> list.map { it.toRecipe() } }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            emptyList()
//        )

    val trendingRecipes = recipeRepo.getTrendingRecipes()
        .map { list -> list.map { it.toRecipe() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ==== NEW DISHES ====
    val newDishes = recipeRepo.getNewDishes()
        .map { list -> list.map { it.toRecipe() } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun toggleFavorite(recipeId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        viewModelScope.launch {

            // Lấy danh sách trending cập nhật hiện tại
            val list = trendingRecipes.value.toMutableList()
            val index = list.indexOfFirst { it.id == recipeId }
            if (index == -1) return@launch

            val recipe = list[index]

            val newStatus = !recipe.isFavorite
            val newCount = if (newStatus) recipe.likeCount + 1 else recipe.likeCount - 1

            // ⭐ OPTIMISTIC UPDATE: đổi UI ngay lập tức
            list[index] = recipe.copy(
                isFavorite = newStatus,
                likeCount = newCount
            )

            // cập nhật xuống Room để stateIn refresh UI
            recipeRepo.updateFavoriteLocal(recipeId, newStatus, newCount)

            // Firestore references
            val recipeRef = firestore.collection("recipes").document(recipeId)
            val userFavRef = firestore.collection("users")
                .document(user.uid)
                .collection("favorites")
                .document(recipeId)

            // ⭐ Transaction Firestore
            firestore.runTransaction { tx ->
                val snap = tx.get(userFavRef)
                if (snap.exists()) {
                    tx.delete(userFavRef)
                    tx.update(recipeRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    false
                } else {
                    tx.set(
                        userFavRef,
                        mapOf(
                            "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "recipeId" to recipeId
                        )
                    )
                    tx.update(recipeRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                    true
                }
            }.addOnSuccessListener { isLiked ->
                // ⭐ lưu local Room thật sự
                viewModelScope.launch {
                    // Provide the optimistic newCount so Room also updates like_count
                    recipeRepo.toggleFavorite(recipeId, isLiked, newCount)
                }
            }
        }
    }

    // ==== USER INFO ====
    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl = _userPhotoUrl.asStateFlow()


    init {
        // 🔥 tải category từ Firestore về Room
        viewModelScope.launch {
            categoryRepo.syncCategories()
        }

        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser
        _userName.value = user?.displayName
        _userPhotoUrl.value = user?.photoUrl?.toString()
    }

    // ==== FACTORY ====
    companion object {
        class Factory(
            private val recipeRepo: RecipeRepository,
            private val categoryDao: CategoryDao
        ) : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val categoryRepository = CategoryRepository(categoryDao)
                return HomeViewModel(recipeRepo, categoryRepository) as T
            }
        }
    }
}