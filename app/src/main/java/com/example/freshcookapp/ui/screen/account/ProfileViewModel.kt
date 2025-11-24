package com.example.freshcookapp.ui.screen.account

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfileUIState(
    val uid: String = "",
    val fullName: String = "Đang tải...",
    val username: String = "",
    val photoUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val recipeCount: Int = 0, // Số này sẽ được đếm tự động
    val isCurrentUser: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UserProfileUIState())
    val uiState: StateFlow<UserProfileUIState> = _uiState

    private var userListener: ListenerRegistration? = null
    private var recipeCountListener: ListenerRegistration? = null

    fun loadProfile(targetUserId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userIdToLoad = if (targetUserId == null || targetUserId == currentUserId || targetUserId == "{userId}") {
            currentUserId
        } else {
            targetUserId
        }
        val isMe = (userIdToLoad == currentUserId)

        // 1. Hủy lắng nghe cũ
        userListener?.remove()
        recipeCountListener?.remove()

        // 2. Lắng nghe thông tin User (Tên, Avatar, Follow...)
        userListener = firestore.collection("users").document(userIdToLoad)
            .addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
                    val fCount = getIntSafe(document, "followerCount")
                    val flwingCount = getIntSafe(document, "followingCount")

                    // Cập nhật thông tin cơ bản trước
                    _uiState.value = _uiState.value.copy(
                        uid = userIdToLoad,
                        fullName = document.getString("fullName") ?: "Người dùng",
                        username = document.getString("email")?.substringBefore("@") ?: "",
                        photoUrl = document.getString("photoUrl"),
                        followerCount = fCount,
                        followingCount = flwingCount,
                        isCurrentUser = isMe
                    )
                }
            }

        // 3. LẮNG NGHE RIÊNG SỐ LƯỢNG MÓN ĂN (Fix lỗi số không tăng)
        // Thay vì đọc trường "dishCount", ta đếm trực tiếp trong bảng recipes
        recipeCountListener = firestore.collection("recipes")
            .whereEqualTo("userId", userIdToLoad) // Lọc món của người này
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    val realCount = snapshot.size() // Đếm số lượng thực tế
                    // Cập nhật vào UI
                    _uiState.value = _uiState.value.copy(recipeCount = realCount)
                }
            }
    }

    private fun getIntSafe(document: DocumentSnapshot, fieldName: String): Int {
        val value = document.get(fieldName)
        return when (value) {
            is Long -> value.toInt()
            is Double -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        recipeCountListener?.remove()
    }
}