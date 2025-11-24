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

    // Cập nhật: Dùng REPLACE để nếu trùng ID từ Firestore thì cập nhật luôn
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity)

    // Mới: Để lưu danh sách tải từ Firestore về
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()

    // Mới: Transaction giúp xóa cũ + thêm mới an toàn
    @Transaction
    suspend fun refreshRecipes(recipes: List<RecipeEntity>) {
        deleteAll()
        insertAll(recipes)
    }

    // 🔥 So sánh với chuỗi 'soup' (vì categoryId giờ là String)
    // Bạn có thể đổi 'soup' thành mã khác nếu muốn
    @Query("SELECT * FROM recipes WHERE category_id = 'soup'")
    fun getRecommendedRecipes(): Flow<List<RecipeEntity>>

    // 🔥Lấy các món KHÔNG phải soup
    @Query("SELECT * FROM recipes WHERE category_id != 'soup'")
    fun getTrendingRecipes(): Flow<List<RecipeEntity>>

    // 🔥Sắp xếp theo thời gian tạo (cột created_at)
    // Nếu cột created_at chưa có dữ liệu thì nó mặc định 0, vẫn chạy được
    @Query("SELECT * FROM recipes ORDER BY created_at DESC LIMIT 10")
    fun getNewDishes(): Flow<List<RecipeEntity>>

    // Tìm kiếm
    @Query("""
        SELECT * FROM recipes
        WHERE name LIKE '%' || :keyword || '%' COLLATE NOCASE
        ORDER BY name
    """)
    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE isFavorite = 1")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    // Lấy danh sách xem gần đây (Sắp xếp mới xem lên đầu)
    @Query("SELECT * FROM recipes WHERE lastViewedTime IS NOT NULL ORDER BY lastViewedTime DESC")
    fun getRecentlyViewedRecipes(): Flow<List<RecipeEntity>>

    // Cập nhật thời gian xem cho một món ăn
    @Query("UPDATE recipes SET lastViewedTime = :timestamp WHERE id = :id")
    suspend fun updateLastViewed(id: String, timestamp: Long)

    // Xóa lịch sử (Set lại thành null)
    @Query("UPDATE recipes SET lastViewedTime = NULL WHERE id = :id")
    suspend fun removeFromHistory(id: String)
}