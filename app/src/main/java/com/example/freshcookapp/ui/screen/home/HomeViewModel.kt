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

    // ==== NEW DISHES ====
    val newDishes = recipeRepo.getNewDishes()
        .map { list -> list.map { it.toRecipe() } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

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