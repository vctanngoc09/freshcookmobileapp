package com.example.freshcookapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "time_cook_minutes")
    val timeCookMinutes: Int? = null,

    @ColumnInfo(name = "people")
    val people: Int? = null,

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    // FOREIGN KEYS
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "category_id")
    val categoryId: Long
)