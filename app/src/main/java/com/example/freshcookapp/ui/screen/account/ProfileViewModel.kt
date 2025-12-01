package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.Recipe
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
    val isCurrentUser: Boolean = false,
    val userRecipes: List<Recipe> = emptyList()
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UserProfileUIState())
    val uiState: StateFlow<UserProfileUIState> = _uiState

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications

    private var userListener: ListenerRegistration? = null
    private var followerListener: ListenerRegistration? = null
    private var followingListener: ListenerRegistration? = null
    private var recipesListener: ListenerRegistration? = null
    private var unreadListener: ListenerRegistration? = null

    fun loadProfile(targetUserId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userIdToLoad = if (targetUserId == null || targetUserId == currentUserId || targetUserId == "{userId}") {
            currentUserId
        } else {
            targetUserId
        }
        val isMe = (userIdToLoad == currentUserId)

        removeListeners()

        // 1. User Info
        userListener = firestore.collection("users").document(userIdToLoad)
            .addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
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

        // 2. User Recipes (FIX LỖI MAPPING TẠI ĐÂY)
        recipesListener = firestore.collection("recipes")
            .whereEqualTo("userId", userIdToLoad)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    val recipeList = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Map thủ công từng trường để tránh lỗi Crash
                            Recipe(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                imageUrl = doc.getString("imageUrl"),
                                timeCook = doc.getLong("timeCook")?.toInt() ?: 0,
                                difficulty = doc.getString("difficulty") ?: "Dễ",
                                author = Author(id = userIdToLoad, name = "", avatarUrl = null),
                                description = "", ingredients = emptyList(), instructions = emptyList(), relatedRecipes = emptyList(), isFavorite = false
                            )
                        } catch (e: Exception) {
                            null // Bỏ qua item lỗi
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        recipeCount = snapshot.size(),
                        userRecipes = recipeList
                    )
                }
            }

        // 3. Follower
        followerListener = firestore.collection("users").document(userIdToLoad)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.value = _uiState.value.copy(followerCount = snapshot.size())
                }
            }

        // 4. Following
        followingListener = firestore.collection("users").document(userIdToLoad)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.value = _uiState.value.copy(followingCount = snapshot.size())
                }
            }

        // 5. Notifications (Only for current user)
        if (isMe) {
            unreadListener = firestore.collection("users").document(currentUserId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        _hasUnreadNotifications.value = snapshot.size() > 0
                    }
                }
        }
    }

    private fun removeListeners() {
        userListener?.remove()
        followerListener?.remove()
        followingListener?.remove()
        recipesListener?.remove()
        unreadListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}