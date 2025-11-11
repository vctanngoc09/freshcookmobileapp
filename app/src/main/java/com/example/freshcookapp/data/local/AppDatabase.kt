package com.example.freshcookapp.data.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freshcookapp.data.local.entity.*
import com.example.freshcookapp.data.local.dao.*
import com.example.freshcookapp.data.local.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        InstructionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): RecipeIngredientDao
    abstract fun instructionDao(): InstructionDao

    companion object {
        fun create(application: Application): AppDatabase {
            return Room.databaseBuilder(
                application,
                AppDatabase::class.java,
                "freshcook.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // INSERT SEED DATA NGAY SAU KHI DB TẠO
                        application.let { app ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = create(app)
                                SeedData.recipes.forEach {
                                    database.recipeDao().insert(it)
                                }
                            }
                        }
                    }
                })
                .build()
        }
    }
}
