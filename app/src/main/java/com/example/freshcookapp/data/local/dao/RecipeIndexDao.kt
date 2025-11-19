package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.RecipeIndexEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIndexDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(indexes: List<RecipeIndexEntity>)

    @Query("""
        SELECT name FROM recipe_index
        WHERE name LIKE '%' || :keyword || '%' COLLATE NOCASE
        LIMIT 10
    """)
    fun suggest(keyword: String): Flow<List<String>>

    @Query("""
        SELECT id FROM recipe_index
        WHERE name = :name
        LIMIT 1
    """)
    suspend fun getIdByName(name: String): String
}
