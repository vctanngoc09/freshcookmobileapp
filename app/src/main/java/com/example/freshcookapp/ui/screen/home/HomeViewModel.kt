package com.example.freshcookapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: RecipeRepository) : ViewModel() {

    private val _recipes = MutableStateFlow(emptyList<com.example.freshcookapp.data.local.entity.RecipeEntity>())
    val recipes: StateFlow<List<com.example.freshcookapp.data.local.entity.RecipeEntity>> = _recipes

    init {
        viewModelScope.launch {
            _recipes.value = repo.getAllRecipes()
        }
    }
}