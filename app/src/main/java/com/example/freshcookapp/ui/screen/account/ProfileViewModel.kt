package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfileUIState(
    val uid: String = "",
    val fullName: String = "Đang tải...",
    val username: String = "",
    val photoUrl: String? = null,
    val followerCount: Int = 0, // Sẽ tự đếm
    val followingCount: Int = 0, // Sẽ tự đếm
    val recipeCount: Int = 0,    // Sẽ tự đếm
    val isCurrentUser: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UserProfileUIState())
    val uiState: StateFlow<UserProfileUIState> = _uiState

    // Các biến để quản lý lắng nghe (tránh rò rỉ bộ nhớ)
    private var userListener: ListenerRegistration? = null
    private var recipeCountListener: ListenerRegistration? = null
    private var followerListener: ListenerRegistration? = null
    private var followingListener: ListenerRegistration? = null

    fun loadProfile(targetUserId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userIdToLoad = if (targetUserId == null || targetUserId == currentUserId || targetUserId == "{userId}") {
            currentUserId
        } else {
            targetUserId
        }
        val isMe = (userIdToLoad == currentUserId)

        // 1. Hủy các lắng nghe cũ trước khi tạo cái mới
        removeListeners()

        // 2. Lắng nghe thông tin cơ bản (Tên, Avatar...)
        userListener = firestore.collection("users").document(userIdToLoad)
            .addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
                    _uiState.value = _uiState.value.copy(
                        uid = userIdToLoad,
                        fullName = document.getString("fullName") ?: "Người dùng",
                        username = document.getString("email")?.substringBefore("@") ?: "",
                        photoUrl = document.getString("photoUrl"),
                        isCurrentUser = isMe
                    )
                }
            }

        // 3. TỰ ĐẾM SỐ LƯỢNG MÓN (Như đã sửa trước đó)
        recipeCountListener = firestore.collection("recipes")
            .whereEqualTo("userId", userIdToLoad)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    _uiState.value = _uiState.value.copy(recipeCount = snapshot.size())
                }
            }

        // 4. TỰ ĐẾM FOLLOWER (Mới thêm)
        // Thay vì tin vào số followerCount, ta đếm trực tiếp trong sub-collection "followers"
        followerListener = firestore.collection("users").document(userIdToLoad)
            .collection("followers")
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    _uiState.value = _uiState.value.copy(followerCount = snapshot.size())
                }
            }

        // 5. TỰ ĐẾM FOLLOWING (Mới thêm)
        followingListener = firestore.collection("users").document(userIdToLoad)
            .collection("following")
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    _uiState.value = _uiState.value.copy(followingCount = snapshot.size())
                }
            }
    }

    private fun removeListeners() {
        userListener?.remove()
        recipeCountListener?.remove()
        followerListener?.remove()
        followingListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}