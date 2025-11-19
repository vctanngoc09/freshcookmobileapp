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
    private val syncRepo = FirestoreSyncRepository(db.recipeIndexDao())

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            syncRepo.syncRecipeIndex()   // ⬅ tải id + name xuống Room
            _isReady.value = true        // báo cho SplashScreen biết đã xong
        }
    }
}
