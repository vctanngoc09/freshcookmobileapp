package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
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
    val recipeCount: Int = 0,
    val isCurrentUser: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UserProfileUIState())
    val uiState: StateFlow<UserProfileUIState> = _uiState

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications

    // Các biến để quản lý lắng nghe
    private var userListener: ListenerRegistration? = null
    private var recipeCountListener: ListenerRegistration? = null
    private var followerListener: ListenerRegistration? = null
    private var followingListener: ListenerRegistration? = null
    private var unreadListener: ListenerRegistration? = null

    fun loadProfile(targetUserId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Xác định xem đang load profile của ai
        val userIdToLoad = if (targetUserId == null || targetUserId == currentUserId || targetUserId == "{userId}") {
            currentUserId
        } else {
            targetUserId
        }
        val isMe = (userIdToLoad == currentUserId)

        removeListeners()

        // 1. Lắng nghe thông tin người dùng (Của userIdToLoad)
        userListener = firestore.collection("users").document(userIdToLoad)
            .addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
                    // SỬA LỖI 1: Ưu tiên lấy username từ DB, nếu null mới fallback sang email
                    val dbUsername = document.getString("username")
                    val emailUsername = document.getString("email")?.substringBefore("@") ?: ""

                    _uiState.value = _uiState.value.copy(
                        uid = userIdToLoad,
                        fullName = document.getString("fullName") ?: "Người dùng",
                        username = if (!dbUsername.isNullOrBlank()) dbUsername else emailUsername,
                        photoUrl = document.getString("photoUrl"),
                        isCurrentUser = isMe
                    )
                }
            }

        // 2. Lắng nghe số lượng món ăn (Của userIdToLoad)
        recipeCountListener = firestore.collection("recipes")
            .whereEqualTo("userId", userIdToLoad)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    _uiState.value = _uiState.value.copy(recipeCount = snapshot.size())
                }
            }

        // 3. Lắng nghe Follower (Của userIdToLoad)
        followerListener = firestore.collection("users").document(userIdToLoad)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.value = _uiState.value.copy(followerCount = snapshot.size())
                }
            }

        // 4. Lắng nghe Following (Của userIdToLoad)
        followingListener = firestore.collection("users").document(userIdToLoad)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.value = _uiState.value.copy(followingCount = snapshot.size())
                }
            }

        // 5. SỬA LỖI 2: LẮNG NGHE THÔNG BÁO CHƯA ĐỌC (LUÔN CỦA CURRENT USER)
        // Dù đang xem profile người khác, badge thông báo vẫn phải là của MÌNH
        unreadListener = firestore.collection("users").document(currentUserId) // <-- Luôn là currentUserId
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    _hasUnreadNotifications.value = snapshot.size() > 0
                }
            }
    }

    private fun removeListeners() {
        userListener?.remove()
        recipeCountListener?.remove()
        followerListener?.remove()
        followingListener?.remove()
        unreadListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}