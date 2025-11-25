package com.example.freshcookapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,                // KHỚP Firestore document.id

    @ColumnInfo(name = "name")
    val name: String,              // Từ Firestore

    @ColumnInfo(name = "image_url")
    val imageUrl: String           // Từ Firestore
)
