package com.example.freshcookapp

import android.app.Application
import androidx.room.Room
import com.example.freshcookapp.data.local.AppDatabase

class FreshCookAppRoom : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "freshcook.db"
        )
            //.allowMainThreadQueries() // Nếu debug muốn cho chạy không cần coroutine
            .build()
    }
}