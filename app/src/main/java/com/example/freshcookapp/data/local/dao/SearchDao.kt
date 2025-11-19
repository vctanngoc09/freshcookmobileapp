package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {

    // 🔍 Gợi ý theo tên món ăn
    @Query("""
        SELECT DISTINCT name FROM recipes
        WHERE name LIKE '%' || :keyword || '%' COLLATE NOCASE
        LIMIT 10
    """)
    fun suggestRecipeNames(keyword: String): Flow<List<String>>
}