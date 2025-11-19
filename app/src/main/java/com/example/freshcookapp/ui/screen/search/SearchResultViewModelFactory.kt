package com.example.freshcookapp.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SearchResultViewModelFactory(
    private val keyword: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchResultViewModel(keyword) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
