package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.RecipeEntity

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes")
    suspend fun getAll(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RecipeEntity?

    @Insert
    suspend fun insert(recipe: RecipeEntity): Long
}
