package com.example.freshcookapp

import android.app.Application
import androidx.room.Room
import com.example.freshcookapp.data.local.AppDatabase

class FreshCookAppRoom : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)
    }
}