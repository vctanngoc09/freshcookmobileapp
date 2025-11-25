package com.example.freshcookapp.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SearchResultViewModelFactory(
    private val keyword: String? = null,
    private val includedIngredients: List<String> = emptyList(),
    private val excludedIngredients: List<String> = emptyList(),
    private val difficulty: String = "",
    private val timeCook: Float = 0f
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchResultViewModel(keyword, includedIngredients, excludedIngredients, difficulty, timeCook) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
