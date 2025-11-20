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

    // Logic cũ: Trả về danh sách TÊN MÓN (List<String>)
    val suggestions: StateFlow<List<String>> =
        _query
            .debounce(200) // Đợi 200ms
            .distinctUntilChanged()
            .flatMapLatest { keyword ->
                if (keyword.isBlank()) {
                    flowOf(emptyList())
                } else {
                    // Gọi Repository để lấy danh sách tên
                    repository.searchRecipes(keyword).map { entities ->
                        entities.map { it.name }
                    }
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
}