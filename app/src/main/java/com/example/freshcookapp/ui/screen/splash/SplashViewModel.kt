package com.example.freshcookapp.ui.screen.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.FirestoreSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Truyền đủ recipeDao VÀ categoryDao
    private val syncRepo = FirestoreSyncRepository(db.recipeDao(), db.categoryDao())

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            // Gọi hàm đồng bộ
            syncRepo.syncRecipes()
            _isReady.value = true
        }
    }
}