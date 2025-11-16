package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.InstructionEntity
import com.example.freshcookapp.data.local.entity.NewDishEntity
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.local.entity.RecipeIngredientEntity
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val db: AppDatabase) {

    fun getTrendingRecipes(): Flow<List<RecipeEntity>> {
        return db.recipeDao().getTrendingRecipes()
    }

    fun getRecommendedRecipes(): Flow<List<RecipeEntity>> {
        return db.recipeDao().getRecommendedRecipes()
    }
    fun getNewDishes(): Flow<List<NewDishEntity>> {
        return db.newDishDao().getAllNewDishes()
    }

    suspend fun saveRecipe(
        name: String,
        description: String,
        timeCookMinutes: Int?,
        people: Int?,
        imageUrl: String?,
        userId: Long,
        categoryId: Long,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>
    ): Long {
        val recipeEntity = RecipeEntity(
            name = name,
            description = description,
            timeCookMinutes = timeCookMinutes,
            people = people,
            imageUrl = imageUrl,
            userId = userId,
            categoryId = categoryId
        )
        val recipeId = db.recipeDao().insert(recipeEntity)

        val ingredientEntities = ingredients.map { ingredient ->
            RecipeIngredientEntity(
                recipeId = recipeId,
                name = ingredient.name,
                quantity = ingredient.quantity,
                unit = ingredient.unit,
                note = ingredient.notes
            )
        }
        db.ingredientDao().insertAll(ingredientEntities)

        val instructionEntities = instructions.map { instruction ->
            InstructionEntity(
                recipeId = recipeId,
                stepNumber = instruction.stepNumber,
                description = instruction.description,
                imageUrl = instruction.imageUrl
            )
        }
        db.instructionDao().insertAll(instructionEntities)

        return recipeId
    }
}