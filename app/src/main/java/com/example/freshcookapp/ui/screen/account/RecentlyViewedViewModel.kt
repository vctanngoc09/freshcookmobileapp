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

    // --- LIST HIỂN THỊ RA UI ---
    val recentlyViewedList: StateFlow<List<ViewedRecipeModel>> =
        repository.getRecentlyViewed()
            .mapLatest { entities ->
                // chạy trên IO để không block main
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

    // Xóa 1 item khỏi lịch sử (theo recipeId)
    fun removeFromHistory(recentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFromRecentlyViewedByRecentId(recentId)
        }
    }

    // Nếu sau này muốn fetch tên thật từ Firestore vẫn dùng được
    private suspend fun fetchAuthorName(userId: String): String {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("fullName") ?: "Người dùng ẩn danh"
        } catch (e: Exception) {
            Log.e("RecentlyViewedVM", "Lỗi fetch tên tác giả: $userId", e)
            "Lỗi tải tên"
        }
    }

    // Map từ RecipeEntity (Room) sang ViewedRecipeModel (UI)
    private suspend fun RecipeRepository.ViewedWithTime.toViewedModel(): ViewedRecipeModel {
        val sdf = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        val timeString = sdf.format(Date(timestamp))

        val authorName = fetchAuthorName(recipe.userId)

        return ViewedRecipeModel(
            recentId = this.recentId,   // INT
            id = recipe.id,             // String
            title = recipe.name,
            authorName = authorName,
            timeViewed = timeString,
            imageUrl = recipe.imageUrl
        )
    }

    // --- FACTORY ---
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
