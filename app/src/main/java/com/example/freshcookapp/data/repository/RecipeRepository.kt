package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RecipeRepository(private val db: AppDatabase) {

    fun getTrendingRecipes() = db.recipeDao().getTrendingRecipes()
    fun getRecommendedRecipes() = db.recipeDao().getRecommendedRecipes()
    fun getNewDishes() = db.recipeDao().getNewDishes()

    // Hàm saveRecipe này dùng cho tạo món Offline (nếu cần)
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