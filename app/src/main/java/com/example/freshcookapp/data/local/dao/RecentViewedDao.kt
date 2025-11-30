package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.RecentViewedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentViewedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecentViewedEntity)

    @Query("""
        SELECT * FROM recent_viewed 
        WHERE userId = :uid 
        ORDER BY timestamp DESC 
        LIMIT 20
    """)
    fun getRecent(uid: String): Flow<List<RecentViewedEntity>>

    @Query("DELETE FROM recent_viewed WHERE recipeId = :recipeId AND userId = :uid")
    suspend fun remove(recipeId: String, uid: String)

    @Query("DELETE FROM recent_viewed WHERE id = :recentId")
    suspend fun removeByRecentId(recentId: Int)

}
