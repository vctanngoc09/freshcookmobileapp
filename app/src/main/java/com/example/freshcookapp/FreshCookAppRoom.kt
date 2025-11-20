package com.example.freshcookapp

import android.app.Application
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.FirestoreSyncRepository
import com.example.freshcookapp.data.sync.FirestoreRealtimeSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FreshCookAppRoom : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)

        // SỬA: Truyền đủ 2 tham số (recipeDao, categoryDao)
        CoroutineScope(Dispatchers.IO).launch {
            FirestoreSyncRepository(database.recipeDao(), database.categoryDao()).syncRecipes()
        }


        FirestoreRealtimeSync(database.recipeDao()).start()
    }
}