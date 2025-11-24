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

    // Logic thông minh: Tự động chuyển đổi giữa Lịch sử và Gợi ý
    val suggestions: StateFlow<List<String>> =
        _query
            .debounce(200) // Đợi người dùng gõ xong 200ms
            .distinctUntilChanged()
            .flatMapLatest { keyword ->
                if (keyword.isBlank()) {
                    // 1. Nếu từ khóa rỗng -> Lấy LỊCH SỬ tìm kiếm
                    repository.getSearchHistory()
                } else {
                    // 2. Nếu có từ khóa -> Tìm tên MÓN ĂN gợi ý
                    repository.suggestNames(keyword)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // Hàm cập nhật text khi gõ
    fun onQueryChange(value: String) {
        _query.value = value
    }

    // Hàm lưu từ khóa khi người dùng chọn
    fun saveSearchQuery(keyword: String) {
        viewModelScope.launch {
            if (keyword.isNotBlank()) {
                repository.saveSearchQuery(keyword)
            }
        }
    }
}