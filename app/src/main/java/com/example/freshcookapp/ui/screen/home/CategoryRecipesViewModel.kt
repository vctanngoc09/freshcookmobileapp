package com.example.freshcookapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.mapper.toRecipe
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryRecipesViewModel(
    private val repo: RecipeRepository
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes = _recipes.asStateFlow()

    fun loadRecipes(categoryId: String) {
        viewModelScope.launch {
            when (categoryId) {
                "TRENDING" -> {
                    repo.getTrendingRecipes().collect { list ->
                        _recipes.value = list.map { it.toRecipe() }
                    }
                }
                "NEW" -> {
                    repo.getNewDishes().collect { list ->
                        _recipes.value = list.map { it.toRecipe() }
                    }
                }
                "RECENTLY_VIEWED" -> {
                    repo.getRecentlyViewed().collect { list ->
                        _recipes.value = list.map { it.toRecipe() }
                    }
                }
                else -> {
                    repo.getRecipesByCategory(categoryId).collect { list ->
                        _recipes.value = list.map { it.toRecipe() }
                    }
                }
            }
        }
    }


    class Factory(private val repo: RecipeRepository) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoryRecipesViewModel::class.java)) {
                return CategoryRecipesViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
