package com.example.freshcookapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val query: String, // Từ khóa là khóa chính (để không bị trùng)
    val timestamp: Long, // Thời gian tìm kiếm (để sắp xếp mới nhất)
    val userId: String
)