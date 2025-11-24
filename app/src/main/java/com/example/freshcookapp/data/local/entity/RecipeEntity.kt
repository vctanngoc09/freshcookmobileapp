package com.example.freshcookapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "time_cook_minutes")
    val timeCookMinutes: Int = 0,

    @ColumnInfo(name = "level")
    val level: String? = "Trung bình",

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    // Lưu danh sách nguyên liệu dạng: "500 g Cánh gà"
    @ColumnInfo(name = "ingredients")
    val ingredients: List<String> = emptyList(),

    // Lưu danh sách các bước dạng: "Bước 1: Ướp gà..."
    @ColumnInfo(name = "steps")
    val steps: List<String> = emptyList(),

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "category_id")
    val categoryId: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,

    @ColumnInfo(defaultValue = "0") // Mặc định là 0 (False - chưa yêu thích)
    val isFavorite: Boolean = false,

    @ColumnInfo(defaultValue = "NULL")
    val lastViewedTime: Long? = null
)