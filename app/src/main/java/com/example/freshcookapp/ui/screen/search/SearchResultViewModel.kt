package com.example.freshcookapp.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.mapper.toRecipe
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer

class SearchResultViewModel(
    private val recipeRepository: RecipeRepository,
    private val keyword: String? = null,
    private val includedIngredients: List<String> = emptyList(),
    private val excludedIngredients: List<String> = emptyList(),
    private val difficulty: String = "",
    private val timeCook: Float = 0f
) : ViewModel() {

    private val _results = MutableStateFlow<List<Recipe>>(emptyList())
    val results = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadResults()
    }

    private fun normalizeText(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    private fun loadResults() {
        viewModelScope.launch {
            _isLoading.value = true
            recipeRepository.searchRecipes(keyword ?: "")
                .map { entityList ->
                    // 1. Chuyển đổi List<RecipeEntity> sang List<Recipe>
                    entityList.map { it.toRecipe() }
                }
                .map { recipeList ->
                    // 2. Lọc trên List<Recipe> đã được chuyển đổi
                    recipeList.filter { recipe ->
                        val normIngredients = recipe.ingredients.map { normalizeText(it) }
                        val hasIncluded = includedIngredients.isEmpty() || includedIngredients.all { normIngredients.contains(normalizeText(it)) }
                        val hasExcluded = excludedIngredients.isEmpty() || excludedIngredients.none { normIngredients.contains(normalizeText(it)) }
                        val difficultyMatches = difficulty.isEmpty() || normalizeText(recipe.difficulty ?: "").contains(normalizeText(difficulty))
                        val timeCookMatches = timeCook == 0f || recipe.timeCook <= timeCook
                        hasIncluded && hasExcluded && difficultyMatches && timeCookMatches
                    }
                }
                .catch { e ->
                    // Xử lý lỗi nếu có
                    _isLoading.value = false
                    _results.value = emptyList()
                }
                .collect { filteredList ->
                    _results.value = filteredList
                    _isLoading.value = false
                }
        }
    }
}
