package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Đây là class chứa dữ liệu (State)
data class UserProfileUIState(
    val uid: String = "",
    val fullName: String = "Đang tải...",
    val username: String = "",
    val photoUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val recipeCount: Int = 0,
    val isCurrentUser: Boolean = false
)

// Đây là ViewModel xử lý logic
class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UserProfileUIState())
    val uiState: StateFlow<UserProfileUIState> = _uiState

    fun loadProfile(targetUserId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Logic xác định xem đang xem ai
        val userIdToLoad = if (targetUserId == null || targetUserId == currentUserId || targetUserId == "{userId}") {
            currentUserId
        } else {
            targetUserId
        }

        val isMe = (userIdToLoad == currentUserId)

        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userIdToLoad).get().await()

                if (userDoc.exists()) {
                    _uiState.value = UserProfileUIState(
                        uid = userIdToLoad,
                        fullName = userDoc.getString("fullName") ?: "Người dùng",
                        username = userDoc.getString("email")?.substringBefore("@") ?: "",
                        photoUrl = userDoc.getString("photoUrl"),
                        followerCount = userDoc.getLong("followerCount")?.toInt() ?: 0,
                        followingCount = userDoc.getLong("followingCount")?.toInt() ?: 0,
                        recipeCount = userDoc.getLong("dishCount")?.toInt() ?: 0,
                        isCurrentUser = isMe
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(fullName = "Lỗi tải dữ liệu")
            }
        }
    }
}