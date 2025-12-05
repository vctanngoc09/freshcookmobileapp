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

    /** Chuẩn hóa text → bỏ dấu, viết thường */
    private fun normalizeText(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    private fun loadResults() {
        viewModelScope.launch {
            _isLoading.value = true

            val sourceFlow =
                if (keyword.isNullOrBlank()) recipeRepository.getAllRecipes()
                else recipeRepository.searchLocal(keyword)

            sourceFlow
                .map { entityList -> entityList.map { it.toRecipe() } }
                .map { recipeList ->

                    recipeList.filter { recipe ->

                        val normIngList = recipe.ingredients.map { normalizeText(it) }

                        // ====== LỌC NGUYÊN LIỆU BAO GỒM ======
                        val includeOk =
                            includedIngredients.isEmpty() ||
                                    includedIngredients.all { include ->
                                        val inc = normalizeText(include)
                                        normIngList.any { ing -> ing.contains(inc) }
                                    }

                        // ====== LỌC NGUYÊN LIỆU LOẠI TRỪ ======
                        val excludeOk =
                            excludedIngredients.isEmpty() ||
                                    excludedIngredients.none { exclude ->
                                        val exc = normalizeText(exclude)
                                        normIngList.any { ing -> ing.contains(exc) }
                                    }

                        // ====== ĐỘ KHÓ ======
                        val difficultyOk =
                            difficulty.isEmpty() ||
                                    normalizeText(recipe.difficulty ?: "")
                                        .contains(normalizeText(difficulty))

                        // ====== THỜI GIAN ======
                        val timeOk =
                            timeCook == 0f || recipe.timeCook <= timeCook

                        includeOk && excludeOk && difficultyOk && timeOk
                    }
                }
                .catch {
                    _results.value = emptyList()
                    _isLoading.value = false
                }
                .collect { filtered ->
                    _results.value = filtered
                    _isLoading.value = false
                }
        }
    }
}