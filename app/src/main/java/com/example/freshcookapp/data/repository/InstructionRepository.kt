package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.AppDatabase

class InstructionRepository(private val db: AppDatabase) {
    suspend fun getByRecipe(recipeId: Long) =
        db.instructionDao().getByRecipe(recipeId)
}