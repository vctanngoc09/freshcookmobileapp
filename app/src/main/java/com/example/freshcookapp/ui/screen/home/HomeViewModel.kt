package com.example.freshcookapp.ui.screen.home

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map // <-- Quan trọng: import map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repo: RecipeRepository,
    categoryDao: CategoryDao
) : ViewModel() {


    val recipes = repo.getTrendingRecipes()
        .map { list -> list.map { it.toRecipe() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )


    val categories = categoryDao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val recommendedRecipes = repo.getRecommendedRecipes()
        .map { list -> list.map { it.toRecipe() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val newDishes = repo.getNewDishes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // Thông tin User
    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()
    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl = _userPhotoUrl.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser
        _userName.value = user?.displayName
        _userPhotoUrl.value = user?.photoUrl?.toString()
    }
}