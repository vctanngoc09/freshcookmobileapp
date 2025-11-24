package com.example.freshcookapp.ui.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FavoriteViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Dùng MutableStateFlow để chúng ta có thể cập nhật thủ công từ Firebase
    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes

    init {
        // Khi ViewModel khởi tạo, tải ngay danh sách yêu thích
        loadFavorites()
    }

    private fun loadFavorites() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            viewModelScope.launch {
                // 1. Lấy danh sách ID món yêu thích từ Firebase
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .get()
                    .await()

                val favoriteIds = snapshot.documents.map { it.id }

                if (favoriteIds.isNotEmpty()) {
                    // 2. Dùng danh sách ID đó để lấy thông tin chi tiết từ Room Database
                    // (Lưu ý: Cách này đòi hỏi Room phải có sẵn thông tin món ăn)
                    // Để đơn giản cho đồ án, ta sẽ load từ Room và lọc thủ công
                    repository.getFavoriteRecipes().collect { localFavorites ->
                        // Cập nhật UI
                        _favoriteRecipes.value = localFavorites.map { it.toUiModel() }

                        // (Nâng cao: Nếu muốn đồng bộ ngược từ Firebase về Room thì viết thêm logic ở đây)
                    }
                } else {
                    _favoriteRecipes.value = emptyList()
                }
            }
        } else {
            // Nếu chưa đăng nhập -> Lấy từ Local
            viewModelScope.launch {
                repository.getFavoriteRecipes().collect { entities ->
                    _favoriteRecipes.value = entities.map { it.toUiModel() }
                }
            }
        }
    }

    fun removeFromFavorites(recipeId: String) {
        val userId = auth.currentUser?.uid
        viewModelScope.launch {
            // Xóa Local
            repository.toggleFavorite(recipeId, false)

            // Xóa Firebase
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .collection("favorites").document(recipeId).delete()
            }

            // Refresh lại list (nếu cần, hoặc Flow sẽ tự update)
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