package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistoryEntity)

    // Lấy lịch sử theo từng user
    @Query("SELECT query FROM search_history WHERE userId = :uid ORDER BY timestamp DESC LIMIT 10")
    fun getSearchHistory(uid: String): Flow<List<String>>

    // Lấy full lịch sử theo user
    @Query("SELECT * FROM search_history WHERE userId = :uid ORDER BY timestamp DESC LIMIT 10")
    fun getAllHistory(uid: String): Flow<List<SearchHistoryEntity>>

    // Xóa lịch sử của user đang đăng nhập
    @Query("DELETE FROM search_history WHERE userId = :uid")
    suspend fun clearAll(uid: String)

    // Xóa 1 query của 1 user
    @Query("DELETE FROM search_history WHERE query = :query AND userId = :uid")
    suspend fun deleteByQuery(query: String, uid: String)
}

