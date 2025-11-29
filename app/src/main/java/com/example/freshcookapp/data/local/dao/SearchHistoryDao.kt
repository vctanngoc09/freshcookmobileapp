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

    @Query("SELECT query FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getSearchHistory(): Flow<List<String>>

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteByQuery(query: String)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getAllHistory(): Flow<List<SearchHistoryEntity>>
}
