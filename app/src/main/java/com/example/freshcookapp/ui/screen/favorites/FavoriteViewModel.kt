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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoriteViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        _isLoading.value = true
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
                            val localRecipes = repository.getRecipesByIds(favoriteIds).associateBy { it.id }
                            val recipesToFetch = favoriteIds.filter { it !in localRecipes }

                            // Update local recipes
                            localRecipes.values.forEach {
                                repository.toggleFavorite(it.id, true)
                            }

                            // Fetch new recipes
                            if (recipesToFetch.isNotEmpty()) {
                                fetchAndSaveRecipesFromFirestore(recipesToFetch)
                            }
                        }
                    }

                    // 3. Sau khi đồng bộ xong, lắng nghe dữ liệu từ Room để hiển thị
                    repository.getFavoriteRecipes().collect { localFavorites ->
                        _favoriteRecipes.value = localFavorites.map { it.toUiModel() }
                        _isLoading.value = false
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

    private suspend fun fetchAndSaveRecipesFromFirestore(recipeIds: List<String>) {
        try {
            val documents = firestore.collection("recipes").whereIn("id", recipeIds).get().await()
            val recipesToInsert = documents.map { document ->
                RecipeEntity(
                    id = document.id,
                    name = document.getString("name") ?: "Món ăn chưa đặt tên",
                    description = document.getString("description"),
                    timeCook = document.getLong("timeCook")?.toInt() ?: 0,
                    difficulty = document.getString("difficulty") ?: "Trung bình",
                    imageUrl = document.getString("imageUrl"),
                    userId = document.getString("userId") ?: "",
                    categoryId = document.getString("categoryId") ?: "",
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    ingredients = (document.get("ingredients") as? List<String>) ?: emptyList(),
                    steps = (document.get("steps") as? List<String>) ?: emptyList(),
                    isFavorite = true
                )
            }
            repository.insertRecipes(recipesToInsert)
        } catch (e: Exception) {
            Log.e("FavoriteViewModel", "Không thể tải món: ${e.message}")
        }
    }

    private fun loadLocalOnly() {
        viewModelScope.launch {
            repository.getFavoriteRecipes().collect { entities ->
                _favoriteRecipes.value = entities.map { it.toUiModel() }
                _isLoading.value = false
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
