package com.example.freshcookapp

import android.app.Application
import android.util.Log
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.FirestoreSyncRepository
import com.example.freshcookapp.data.sync.FirestoreRealtimeSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.FirebaseApp

class FreshCookAppRoom : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        // Ensure Firebase is initialized early so other Firebase APIs (Firestore, Auth, Messaging)
        // can be used safely during Application startup (e.g., realtime sync or token updates).
        FirebaseApp.initializeApp(this)

        database = AppDatabase.getDatabase(this)

        // --- QUAN TRỌNG: TẠM THỜI COMMENT ĐOẠN NÀY LẠI ---
        // Lý do: Nó đang tải lại dữ liệu gốc từ Server và xóa mất trạng thái Yêu thích của bạn

        CoroutineScope(Dispatchers.IO).launch {
            FirestoreSyncRepository(database.recipeDao(), database.categoryDao()).syncRecipes()
        }
        try {
            FirestoreRealtimeSync(database.recipeDao()).start()
        } catch (e: Exception) {
            Log.e("FreshCookAppRoom", "Realtime sync failed to start", e)
        }

    }
}