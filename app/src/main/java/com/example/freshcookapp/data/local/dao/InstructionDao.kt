package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.InstructionEntity

@Dao
interface InstructionDao {

    @Query("SELECT * FROM instructions WHERE recipe_id = :recipeId ORDER BY step_number ASC")
    suspend fun getByRecipe(recipeId: Long): List<InstructionEntity>

    @Insert
    suspend fun insertAll(list: List<InstructionEntity>)
}
