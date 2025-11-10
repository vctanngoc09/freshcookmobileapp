package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.RecipeIngredientEntity

@Dao
interface RecipeIngredientDao {

    @Query("SELECT * FROM recipe_ingredients WHERE recipe_id = :recipeId")
    suspend fun getByRecipe(recipeId: Long): List<RecipeIngredientEntity>

    @Insert
    suspend fun insertAll(list: List<RecipeIngredientEntity>)
}
