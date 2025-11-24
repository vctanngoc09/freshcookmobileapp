package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RecipeRepository(private val db: AppDatabase) {

    // --- CÁC HÀM LẤY DATA TRANG HOME ---
    fun getTrendingRecipes() = db.recipeDao().getTrendingRecipes()
    fun getRecommendedRecipes() = db.recipeDao().getRecommendedRecipes()
    fun getNewDishes() = db.recipeDao().getNewDishes()

    // --- CÁC HÀM MỚI THÊM (CHO FAVORITE & DETAIL) ---

    // 1. Lấy chi tiết một món ăn bằng ID
    suspend fun getRecipeById(id: String): RecipeEntity? {
        return db.recipeDao().getRecipeById(id)
    }

    // 2. Lấy danh sách các món đã thả tim
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>> {
        return db.recipeDao().getFavoriteRecipes()
    }

    // 3. Cập nhật trạng thái yêu thích
    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        db.recipeDao().updateFavoriteStatus(recipeId, isFavorite)
    }

    // Gọi hàm này khi vào xem chi tiết (Lịch sử)
    suspend fun addToHistory(recipeId: String) {
        db.recipeDao().updateLastViewed(recipeId, System.currentTimeMillis())
    }

    // Gọi hàm này ở trang Xem gần đây
    fun getRecentlyViewed(): Flow<List<RecipeEntity>> {
        return db.recipeDao().getRecentlyViewedRecipes()
    }

    // Xóa 1 món khỏi lịch sử
    suspend fun removeFromHistory(recipeId: String) {
        db.recipeDao().removeFromHistory(recipeId)
    }

    // --- SỬA LỖI Ở ĐÂY ---
    // Thêm cặp ngoặc () sau recipeDao
    fun getRelatedRecipes(categoryId: String, currentId: String): Flow<List<RecipeEntity>> {
        return db.recipeDao().getRelatedRecipes(categoryId, currentId)
    }

    // --- HÀM TẠO MÓN ---
    suspend fun saveRecipe(
        name: String,
        description: String,
        timeCookMinutes: Int?,
        people: Int?,
        imageUrl: String?,
        userId: String,
        categoryId: String,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>
    ) {
        val ingredientsList = ingredients.map { "${it.quantity} ${it.unit} ${it.name}" }
        val stepsList = instructions.map { "Bước ${it.stepNumber}: ${it.description}" }

        val recipeEntity = RecipeEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            timeCookMinutes = timeCookMinutes ?: 0,
            imageUrl = imageUrl,
            userId = userId,
            categoryId = categoryId,
            ingredients = ingredientsList,
            steps = stepsList,
            createdAt = System.currentTimeMillis()
        )
        db.recipeDao().insert(recipeEntity)
    }
}