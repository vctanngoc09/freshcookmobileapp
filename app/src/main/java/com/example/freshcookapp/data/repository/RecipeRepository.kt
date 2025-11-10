package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.RecipeEntity

class RecipeRepository(private val db: AppDatabase) {

    suspend fun getAllRecipes(): List<RecipeEntity> =
        db.recipeDao().getAll()

    suspend fun getRecipeDetail(id: Long) =
        db.recipeDao().getById(id)

}
