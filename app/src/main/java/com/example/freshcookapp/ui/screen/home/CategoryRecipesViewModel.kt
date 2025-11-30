package com.example.freshcookapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.mapper.toRecipe
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CategoryRecipesViewModel(
    private val repo: RecipeRepository
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _endReached = MutableStateFlow(false)
    val endReached = _endReached.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10
    private var currentCategoryId: String? = null

    /**
     * Gọi khi màn "Xem tất cả" được mở.
     */
    fun start(categoryId: String) {
        // Nếu cùng category và đã có data thì không reload
        if (currentCategoryId == categoryId && _recipes.value.isNotEmpty()) return

        currentCategoryId = categoryId
        currentPage = 0
        _recipes.value = emptyList()
        _endReached.value = false

        loadNextPage()
    }

    /**
     * Load page tiếp theo (10 món).
     * Được gọi khi:
     * - Lần đầu mở màn
     * - Kéo tới cuối list.
     */
    fun loadNextPage() {
        val categoryId = currentCategoryId ?: return
        if (_isLoading.value || _endReached.value) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // RECENTLY_VIEWED: không phân trang, lấy 1 lần là đủ
                if (categoryId == "RECENTLY_VIEWED") {
                    val history = repo.getRecentlyViewed().first()
                    _recipes.value = history.map { it.recipe.toRecipe() }
                    _endReached.value = true
                    return@launch
                }

                val offset = currentPage * pageSize
                val entities = when (categoryId) {
                    "TRENDING" -> repo.getTrendingPage(pageSize, offset)
                    "NEW" -> repo.getNewDishesPage(pageSize, offset)
                    else -> repo.getRecipesByCategoryPage(categoryId, pageSize, offset)
                }

                if (entities.isEmpty()) {
                    _endReached.value = true
                } else {
                    val mapped = entities.map { it.toRecipe() }
                    _recipes.value = _recipes.value + mapped
                    currentPage++
                }
            } finally {
                _isLoading.value = false
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