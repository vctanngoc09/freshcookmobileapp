package com.example.freshcookapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.freshcookapp.data.local.entity.*
import com.example.freshcookapp.data.local.dao.*

@Database(
    entities = [
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        InstructionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): RecipeIngredientDao
    abstract fun instructionDao(): InstructionDao
}
