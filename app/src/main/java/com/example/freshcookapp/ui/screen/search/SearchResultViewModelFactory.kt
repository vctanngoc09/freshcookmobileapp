package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository

class SearchResultViewModelFactory(
    private val application: Application,
    private val keyword: String?,
    private val includedIngredients: List<String>,
    private val excludedIngredients: List<String>,
    private val difficulty: String,
    private val timeCook: Float
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            val recipeRepository = RecipeRepository(db)
            @Suppress("UNCHECKED_CAST")
            return SearchResultViewModel(
                recipeRepository,
                keyword,
                includedIngredients,
                excludedIngredients,
                difficulty,
                timeCook
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
