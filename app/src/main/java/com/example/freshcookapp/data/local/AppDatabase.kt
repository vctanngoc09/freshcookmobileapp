package com.example.freshcookapp.data.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freshcookapp.data.local.dao.*
import com.example.freshcookapp.data.local.entity.*

@Database(
    entities = [
        RecipeIndexEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        InstructionEntity::class,
        CategoryEntity::class,
        NewDishEntity::class,
        SearchHistoryEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): RecipeIngredientDao
    abstract fun instructionDao(): InstructionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun newDishDao(): NewDishDao
    abstract fun recipeIndexDao(): RecipeIndexDao

    // Giữ nguyên SearchDao cũ (nếu dùng cho FTS/tìm kiếm món ăn)
    abstract fun searchDao(): SearchDao

    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(application: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    AppDatabase::class.java,
                    "freshcook.db"
                )
                    .fallbackToDestructiveMigration() // Cho phép xóa data cũ để tạo bảng mới (tránh crash)
                    .addCallback(AppDatabaseCallback(application))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val application: Application
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }
}