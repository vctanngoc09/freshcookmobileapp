package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.SearchRepository

class SearchViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)

            // CẬP NHẬT: Truyền đủ 2 tham số (RecipeDao và SearchHistoryDao)
            val repository = SearchRepository(
                recipeDao = db.recipeDao(),
                historyDao = db.searchHistoryDao() // Thêm cái này
            )

            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}