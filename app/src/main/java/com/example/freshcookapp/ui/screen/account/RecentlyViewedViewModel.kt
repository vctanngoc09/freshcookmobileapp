package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentlyViewedViewModel(private val repository: RecipeRepository) : ViewModel() {

    // Lấy danh sách từ DB và chuyển đổi sang Model UI
    val recentlyViewedList: StateFlow<List<ViewedRecipeModel>> = repository.getRecentlyViewed()
        .map { entities -> entities.map { it.toViewedModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hàm xóa khỏi lịch sử
    fun removeFromHistory(id: String) {
        viewModelScope.launch {
            repository.removeFromHistory(id)
        }
    }

    // Chuyển đổi Entity -> Model UI
    private fun RecipeEntity.toViewedModel(): ViewedRecipeModel {
        // Format thời gian (Ví dụ: 14:30 24/11)
        val sdf = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        val timeString = this.lastViewedTime?.let { sdf.format(Date(it)) } ?: "Vừa xong"

        return ViewedRecipeModel(
            id = this.id,
            title = this.name,
            authorName = "Admin", // Hoặc lấy từ DB nếu có
            timeViewed = timeString,
            imageUrl = this.imageUrl
        )
    }
}