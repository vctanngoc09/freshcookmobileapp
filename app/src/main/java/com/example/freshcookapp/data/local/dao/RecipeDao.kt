package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recipe: RecipeEntity): Long

    // Hàm mới: Lấy các món Gợi ý (ID = 100)
    @Query("SELECT * FROM recipes WHERE category_id = 100")
    fun getRecommendedRecipes(): Flow<List<RecipeEntity>>

    // Hàm mới: Lấy các món Xu hướng (KHÔNG phải ID 100)
    @Query("SELECT * FROM recipes WHERE category_id != 100")
    fun getTrendingRecipes(): Flow<List<RecipeEntity>>

    // 🔍 Tìm món ăn theo tên (dùng cho màn kết quả tìm kiếm)
    @Query("""
        SELECT * FROM recipes
        WHERE name LIKE '%' || :keyword || '%' COLLATE NOCASE
        ORDER BY name
    """)
    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>>
}