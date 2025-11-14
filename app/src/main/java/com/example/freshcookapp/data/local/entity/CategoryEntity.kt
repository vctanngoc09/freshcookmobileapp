package com.example.freshcookapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = false) // Chúng ta sẽ tự điền ID từ SeedData
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "key") // Dùng để tham chiếu, vd: "thit", "banh"
    val key: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String // Quan trọng: Đổi từ R.drawable sang String URL
)