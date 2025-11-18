package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchResultViewModel(
    private val keyword: String,
    application: Application
) : ViewModel() {

    private val recipeDao = AppDatabase.getDatabase(application).recipeDao()

    val results: StateFlow<List<RecipeEntity>> =
        recipeDao.searchRecipes(keyword)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
}

class SearchResultViewModelFactory(
    private val keyword: String,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchResultViewModel(keyword, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
