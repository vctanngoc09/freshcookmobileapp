package com.example.freshcookapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcookapp.data.local.entity.NewDishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewDishDao {

    @Query("SELECT * FROM new_dishes")
    fun getAllNewDishes(): Flow<List<NewDishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dishes: List<NewDishEntity>)
}