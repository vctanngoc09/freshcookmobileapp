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

    // Lưu danh sách nguyên liệu
    @ColumnInfo(name = "ingredients")
    val ingredients: List<String> = emptyList(),

    // Lưu danh sách các bước
    @ColumnInfo(name = "steps")
    val steps: List<String> = emptyList(),

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    // --- QUAN TRỌNG: Cột này để lọc món tương tự ---
    @ColumnInfo(name = "category_id")
    val categoryId: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,

    // --- Cột trạng thái Yêu thích ---
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,

    // --- Cột thời gian xem gần đây ---
    @ColumnInfo(name = "last_viewed_time", defaultValue = "NULL")
    val lastViewedTime: Long? = null
)