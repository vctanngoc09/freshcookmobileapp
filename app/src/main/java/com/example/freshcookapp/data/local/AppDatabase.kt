package com.example.freshcookapp.data.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freshcookapp.data.local.entity.*
import com.example.freshcookapp.data.local.dao.*
//import com.example.freshcookapp.data.local.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.TypeConverters

@Database(
    entities = [
        RecipeIndexEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        InstructionEntity::class,
        CategoryEntity::class,
        NewDishEntity::class
    ],
    version = 5,
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

    abstract fun searchDao(): SearchDao
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
                    .fallbackToDestructiveMigration()
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
                /*CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(application)


                    SeedData.recipes.forEach {
                        database.recipeDao().insert(it)
                    }

                    database.categoryDao().insertAll(SeedData.categories)

                    database.newDishDao().insertAll(SeedData.newDishes)
                }*/
            }
        }
    }
}