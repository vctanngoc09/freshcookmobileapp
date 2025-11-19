package com.example.freshcookapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_index")
data class RecipeIndexEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String
)
