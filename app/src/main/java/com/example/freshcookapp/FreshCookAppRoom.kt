package com.example.freshcookapp

import android.app.Application
import androidx.room.Room
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

        val db = AppDatabase.getDatabase(this)
        // Sync 1 lần khi khởi động app
        CoroutineScope(Dispatchers.IO).launch {
            FirestoreSyncRepository(db.recipeIndexDao()).syncRecipeIndex()
        }

        // 🔥 Sync realtime
        FirestoreRealtimeSync(db.recipeIndexDao()).start()
    }
}