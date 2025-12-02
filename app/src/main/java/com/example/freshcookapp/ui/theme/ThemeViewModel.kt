package com.example.freshcookapp.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    init {
        viewModelScope.launch {
            ThemePreferences.getTheme(application).collect {
                _themeMode.value = it
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            ThemePreferences.setTheme(getApplication(), mode)
        }
    }
}