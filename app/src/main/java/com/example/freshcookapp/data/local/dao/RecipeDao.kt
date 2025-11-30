package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.freshcookapp.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()

    @Transaction
    suspend fun refreshRecipes(recipes: List<RecipeEntity>) {
        deleteAll()
        insertAll(recipes)
    }

    // Các hàm lấy danh sách hiển thị (Đã đúng tên cột category_id)
    @Query("SELECT * FROM recipes WHERE category_id = :categoryId")
    fun getRecipesByCategory(categoryId: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes ORDER BY created_at DESC LIMIT 10")
    fun getNewDishes(): Flow<List<RecipeEntity>>

    // Tìm kiếm
    @Query("""
        SELECT * FROM recipes
        WHERE name LIKE '%' || :keyword || '%' COLLATE NOCASE
        ORDER BY name
    """)
    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>


    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE id IN (:ids)")
    suspend fun getRecipesByIds(ids: List<String>): List<RecipeEntity>

    // Live flow for a single recipe so screens can observe updates from Room
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeByIdFlow(id: String): kotlinx.coroutines.flow.Flow<RecipeEntity?>


    // --- SỬA Lỗi: isFavorite -> is_favorite ---
    @Query("SELECT * FROM recipes WHERE is_favorite = 1")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("UPDATE recipes SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean): Int

    // --- SỬA Lỗi: lastViewedTime -> last_viewed_time ---
    @Query("SELECT * FROM recipes WHERE last_viewed_time IS NOT NULL ORDER BY last_viewed_time DESC")
    fun getRecentlyViewedRecipes(): Flow<List<RecipeEntity>>

    @Query("UPDATE recipes SET last_viewed_time = :timestamp WHERE id = :id")
    suspend fun updateLastViewed(id: String, timestamp: Long)

    @Query("UPDATE recipes SET last_viewed_time = NULL WHERE id = :id")
    suspend fun removeFromHistory(id: String)

    // --- SỬA Lỗi: categoryId -> category_id ---
    @Query("SELECT * FROM recipes WHERE category_id = :categoryId AND id != :currentId LIMIT 5")
    fun getRelatedRecipes(categoryId: String, currentId: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes ORDER BY like_count DESC LIMIT 10")
    fun getTrendingRecipes(): Flow<List<RecipeEntity>>

    @Query("""
    UPDATE recipes 
    SET is_favorite = :isFavorite, like_count = :likeCount
    WHERE id = :recipeId
""")
    suspend fun updateFavoriteLocal(
        recipeId: String,
        isFavorite: Boolean,
        likeCount: Int
    )
    // --- HÀM MỚI CHO LOGOUT ---
    @Query("UPDATE recipes SET is_favorite = 0 WHERE is_favorite = 1")
    suspend fun clearAllFavorites()

    @Query("UPDATE recipes SET last_viewed_time = NULL WHERE last_viewed_time IS NOT NULL")
    suspend fun clearHistory()

    @Query("""
    UPDATE recipes SET 
        ingredients = :ingredients,
        steps = :steps,
        description = :description,
        author_name = :authorName,
        author_avatar = :authorAvatar
    WHERE id = :id
""")
    suspend fun updateDetailPart(
        id: String,
        ingredients: List<String>,
        steps: List<String>,
        description: String,
        authorName: String,
        authorAvatar: String
    )


    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :keyword || '%' LIMIT 10")
    fun searchByName(keyword: String): List<RecipeEntity>


}