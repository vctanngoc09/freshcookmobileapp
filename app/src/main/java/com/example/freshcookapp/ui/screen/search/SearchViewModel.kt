package com.example.freshcookapp.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.SearchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val suggestions: StateFlow<List<String>> =
        _query
            .debounce(200)
            .distinctUntilChanged()
            .flatMapLatest { keyword ->
                if (keyword.isBlank()) {
                    repository.getSearchHistory()
                } else {
                    repository.suggestNames(keyword)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun saveSearchQuery(keyword: String) {
        viewModelScope.launch {
            if (keyword.isNotBlank()) {
                repository.saveSearchQuery(keyword)
            }
        }
    }

    fun deleteSearchQuery(keyword: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(keyword)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}
