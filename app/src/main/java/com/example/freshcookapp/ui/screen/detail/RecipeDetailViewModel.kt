package com.example.freshcookapp.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.InstructionStep
import com.example.freshcookapp.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    // 1. Hàm load dữ liệu từ DB
    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            // 1. Lấy dữ liệu hiển thị (như cũ)
            val entity = repository.getRecipeById(recipeId)
            _recipe.value = entity?.toUiModel()

            // 2. THÊM DÒNG NÀY: Lưu vào lịch sử xem ngay lập tức
            if (entity != null) {
                repository.addToHistory(recipeId)
            }
        }
    }

    // 2. Hàm xử lý Yêu thích (Thả tim)
    fun toggleFavorite() {
        val currentRecipe = _recipe.value ?: return
        val newStatus = !currentRecipe.isFavorite

        viewModelScope.launch {
            // Cập nhật vào DB
            repository.toggleFavorite(currentRecipe.id, newStatus)

            // Cập nhật ngay lập tức trên giao diện
            _recipe.value = currentRecipe.copy(isFavorite = newStatus)
        }
    }

    // Hàm chuyển đổi Entity -> Model UI
    private fun RecipeEntity.toUiModel(): Recipe {
        return Recipe(
            id = this.id,
            title = this.name,
            time = "${this.timeCookMinutes} phút",
            level = "Trung bình",
            imageRes = null,
            imageUrl = this.imageUrl, // Quan trọng: Dùng link ảnh
            author = Author(this.userId, "Admin", ""),
            description = this.description ?: "",
            isFavorite = this.isFavorite, // Lấy trạng thái yêu thích thật từ DB
            ingredients = this.ingredients,
            // Chuyển đổi chuỗi hướng dẫn thành List object
            instructions = this.steps.mapIndexed { index, desc ->
                InstructionStep(index + 1, desc, null)
            },
            hashtags = listOf("#Ngon", "#Mới"),
            relatedRecipes = emptyList()
        )
    }
}