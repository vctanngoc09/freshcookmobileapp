package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    // Lưu từ khóa (Nếu trùng thì ghi đè để cập nhật thời gian mới nhất)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistoryEntity)

    // Lấy danh sách lịch sử (Mới nhất lên đầu, lấy 10 cái)
    @Query("SELECT query FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getSearchHistory(): Flow<List<String>>

    // Xóa tất cả lịch sử
    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}