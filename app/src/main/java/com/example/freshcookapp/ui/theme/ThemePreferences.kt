package com.example.freshcookapp.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

enum class ThemeMode(val value: Int) {
    LIGHT(0),
    DARK(1),
    SYSTEM(2)
}

val Context.dataStore by preferencesDataStore("app_theme")

object ThemePreferences {

    private val THEME_KEY = intPreferencesKey("theme_mode")

    suspend fun setTheme(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.value
        }
    }

    fun getTheme(context: Context) = context.dataStore.data.map { prefs ->
        val value = prefs[THEME_KEY] ?: ThemeMode.SYSTEM.value
        ThemeMode.values().first { it.value == value }
    }
}