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

class FavoriteViewModel(repository: RecipeRepository) : ViewModel() {

    // Lấy dữ liệu từ DB và chuyển đổi (Map) sang model UI
    val favoriteRecipes: StateFlow<List<Recipe>> = repository.getFavoriteRecipes()
        .map { entities ->
            entities.map { it.toUiModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Hàm chuyển đổi từ Entity (Database) -> Recipe (Giao diện)
    // Lưu ý: Bạn cần kiểm tra lại các trường trong model Recipe của bạn
    private fun RecipeEntity.toUiModel(): Recipe {
        return Recipe(
            id = this.id,
            title = this.name,
            time = "${this.timeCookMinutes} phút",
            level = "Dễ", // Hoặc lấy từ DB nếu có cột level
            imageRes = null, // Không dùng ảnh local nữa
            imageUrl = this.imageUrl, // Dùng ảnh từ link/db
            author = com.example.freshcookapp.domain.model.Author("1", "Admin", ""), // Fake tạm author
            description = this.description ?: "",
            isFavorite = true, // Vì đang ở màn hình Favorite nên chắc chắn là true
            ingredients = this.ingredients,
            instructions = emptyList(), // Danh sách này chỉ cần khi vào chi tiết
            hashtags = emptyList(),
            relatedRecipes = emptyList()
        )
    }
}