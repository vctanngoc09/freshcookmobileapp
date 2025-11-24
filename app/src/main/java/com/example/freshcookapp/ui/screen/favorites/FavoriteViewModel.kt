package com.example.freshcookapp.ui.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repository: RecipeRepository) : ViewModel() {

    val favoriteRecipes: StateFlow<List<Recipe>> = repository.getFavoriteRecipes()
        .map { entities ->
            entities.map { it.toUiModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ĐÂY LÀ HÀM BỊ THIẾU GÂY LỖI ---
    fun removeFromFavorites(recipeId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId, false)
        }
    }

    private fun RecipeEntity.toUiModel(): Recipe {
        return Recipe(
            id = this.id,
            title = this.name,
            time = "${this.timeCookMinutes} phút",
            level = "Dễ",
            imageRes = null,
            imageUrl = this.imageUrl,
            description = this.description ?: "",
            author = com.example.freshcookapp.domain.model.Author("1", "Admin", ""),
            isFavorite = true,
            ingredients = this.ingredients,
            instructions = emptyList(),
            hashtags = emptyList(),
            relatedRecipes = emptyList()
        )
    }
}