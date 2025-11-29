package com.example.freshcookapp.ui.screen.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentlyViewedViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // --- SỬA LẠI LOGIC MAP Ở ĐÂY ---
    val recentlyViewedList: StateFlow<List<ViewedRecipeModel>> = repository.getRecentlyViewed()
        // Sử dụng mapLatest để xử lý suspend function một cách an toàn
        .mapLatest { entities ->
            // Chạy việc chuyển đổi trên Coroutine Worker
            withContext(Dispatchers.IO) {
                entities.map { entity ->
                    entity.toViewedModel()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeFromHistory(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFromHistory(id)
        }
    }

    private suspend fun fetchAuthorName(userId: String): String {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("fullName") ?: "Người dùng ẩn danh"
        } catch (e: Exception) {
            Log.e("RecentlyViewedVM", "Lỗi fetch tên tác giả: $userId", e)
            "Lỗi tải tên"
        }
    }

    private suspend fun RecipeEntity.toViewedModel(): ViewedRecipeModel {
        val sdf = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        val timeString = this.lastViewedTime?.let { sdf.format(Date(it)) } ?: "Vừa xong"

        val authorName = fetchAuthorName(this.userId)

        return ViewedRecipeModel(
            id = this.id,
            title = this.name,
            authorName = authorName,
            timeViewed = timeString,
            imageUrl = this.imageUrl
        )
    }

    // --- THÊM FACTORY ---
    companion object {
        fun provideFactory(db: AppDatabase): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecentlyViewedViewModel(RecipeRepository(db)) as T
                }
            }
        }
    }
}
