package com.example.freshcookapp.ui.screen.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoriteViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            viewModelScope.launch {
                try {
                    // 1. Lấy danh sách ID món yêu thích từ User
                    val snapshot = firestore.collection("users")
                        .document(userId)
                        .collection("favorites")
                        .get()
                        .await()

                    val favoriteIds = snapshot.documents.map { it.id }

                    // 2. ĐỒNG BỘ DỮ LIỆU
                    if (favoriteIds.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            favoriteIds.forEach { recipeId ->
                                // Nếu đã có bản ghi trong Room thì chỉ cần đánh dấu is_favorite = true
                                val local = repository.getRecipeById(recipeId)
                                if (local != null) {
                                    // Chỉ update flag (không thay đổi likeCount tại bước sync này)
                                    repository.toggleFavorite(recipeId, true)
                                } else {
                                    // Nếu chưa có trong local -> tải chi tiết từ Firestore rồi lưu (đã set isFavorite = true khi lưu)
                                    fetchAndSaveRecipeFromFirestore(recipeId)
                                }
                            }
                        }
                    }

                    // 3. Sau khi đồng bộ xong, lắng nghe dữ liệu từ Room để hiển thị
                    repository.getFavoriteRecipes().collect { localFavorites ->
                        _favoriteRecipes.value = localFavorites.map { it.toUiModel() }
                    }

                } catch (e: Exception) {
                    Log.e("FavoriteViewModel", "Lỗi sync: ${e.message}")
                    // Nếu lỗi mạng, vẫn load những gì đang có trong máy
                    loadLocalOnly()
                }
            }
        } else {
            loadLocalOnly()
        }
    }

    // Hàm phụ: Tải chi tiết món ăn từ bộ sưu tập "recipes" trên Firestore
    private suspend fun fetchAndSaveRecipeFromFirestore(recipeId: String) {
        try {
            val document = firestore.collection("recipes").document(recipeId).get().await()

            if (document.exists()) {
                // Parse dữ liệu từ Firestore thành RecipeEntity
                // Lưu ý: Tên trường (field) phải khớp với trên Firestore của bạn
                val entity = RecipeEntity(
                    id = document.id,
                    name = document.getString("name") ?: "Món ăn chưa đặt tên",
                    description = document.getString("description"),
                    timeCook = document.getLong("timeCook")?.toInt() ?: 0,
                    difficulty = document.getString("difficulty") ?: "Trung bình",
                    imageUrl = document.getString("imageUrl"),
                    userId = document.getString("userId") ?: "",
                    categoryId = document.getString("categoryId") ?: "",
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),

                    // Xử lý mảng (List)
                    ingredients = (document.get("ingredients") as? List<String>) ?: emptyList(),
                    steps = (document.get("steps") as? List<String>) ?: emptyList(),

                    // Quan trọng: Đánh dấu là yêu thích luôn khi lưu vào
                    isFavorite = true
                )

                // Lưu vào Room
                repository.insertRecipe(entity)
                Log.d("FavoriteViewModel", "Đã tải và lưu món: ${entity.name}")
            }
        } catch (e: Exception) {
            Log.e("FavoriteViewModel", "Không thể tải món $recipeId: ${e.message}")
        }
    }

    private fun loadLocalOnly() {
        viewModelScope.launch {
            repository.getFavoriteRecipes().collect { entities ->
                _favoriteRecipes.value = entities.map { it.toUiModel() }
            }
        }
    }

    fun removeFromFavorites(recipeId: String) {
        val userId = auth.currentUser?.uid
        viewModelScope.launch {
            // Xóa Local (chỉ cần update flag = false)
            repository.toggleFavorite(recipeId, false)

            // Xóa Firebase
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .collection("favorites").document(recipeId).delete()
            }
        }
    }

    private fun RecipeEntity.toUiModel(): Recipe {
        return Recipe(
            id = this.id,
            name = this.name,
            timeCook = this.timeCook,
            difficulty = this.difficulty ?: "Dễ",
            imageUrl = this.imageUrl,
            description = this.description ?: "",
            author = com.example.freshcookapp.domain.model.Author("1", "Admin", ""),
            isFavorite = true,
            ingredients = this.ingredients,
            instructions = emptyList(), // Bạn có thể map steps sang instructions nếu cần
            hashtags = emptyList(),
            relatedRecipes = emptyList()
        )
    }
}