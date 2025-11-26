package com.example.freshcookapp.ui.screen.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentlyViewedViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // Lấy danh sách từ DB và chuyển đổi sang Model UI
    val recentlyViewedList: StateFlow<List<ViewedRecipeModel>> = repository.getRecentlyViewed()
        // Map list entities sang list ViewModels
        .map { entities ->
            entities.map { it.toViewedModel() }
        }
        // Quan trọng: Sử dụng Dispatchers.IO để chạy map bất đồng bộ (fetch tên)
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hàm xóa khỏi lịch sử
    fun removeFromHistory(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFromHistory(id)
        }
    }

    // Hàm phụ: Lấy tên tác giả từ Firebase
    private suspend fun fetchAuthorName(userId: String): String {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("fullName") ?: "Người dùng ẩn danh"
        } catch (e: Exception) {
            Log.e("RecentlyViewedVM", "Lỗi fetch tên tác giả: $userId", e)
            "Lỗi tải tên"
        }
    }

    // Chuyển đổi Entity -> Model UI (Đã là hàm suspend)
    private suspend fun RecipeEntity.toViewedModel(): ViewedRecipeModel {
        // Format thời gian
        val sdf = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        val timeString = this.lastViewedTime?.let { sdf.format(Date(it)) } ?: "Vừa xong"

        // --- GỌI HÀM BẤT ĐỒNG BỘ ĐỂ LẤY TÊN THẬT ---
        val authorName = fetchAuthorName(this.userId)
        // ------------------------------------------

        return ViewedRecipeModel(
            id = this.id,
            title = this.name,
            authorName = authorName, // <--- ĐÃ FIX: Dùng tên thật
            timeViewed = timeString,
            imageUrl = this.imageUrl
        )
    }
}