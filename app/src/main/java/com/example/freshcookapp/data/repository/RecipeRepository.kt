package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.NewDishEntity
import com.example.freshcookapp.data.local.entity.RecipeEntity
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
}