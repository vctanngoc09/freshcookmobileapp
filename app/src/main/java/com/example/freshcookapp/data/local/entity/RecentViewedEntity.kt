package com.example.freshcookapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_viewed")
data class RecentViewedEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: String,
    val userId: String,
    val timestamp: Long
)

