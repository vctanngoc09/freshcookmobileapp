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
            repo.getRecipesByCategory(categoryId).collect { list ->
                _recipes.value = list.map { it.toRecipe() }
            }
        }
    }


    class Factory(private val repo: RecipeRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryRecipesViewModel(repo) as T
        }
    }
}
