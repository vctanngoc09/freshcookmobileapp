package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AuthorProfileUIState(
    val uid: String = "",
    val fullName: String = "Đang tải...",
    val username: String = "",
    val photoUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val recipeCount: Int = 0,
    val isFollowing: Boolean = false,
    val authorRecipes: List<Recipe> = emptyList() // Danh sách món ăn
)

class AuthorProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthorProfileUIState())
    val uiState: StateFlow<AuthorProfileUIState> = _uiState

    private var userListener: ListenerRegistration? = null
    private var followStatusListener: ListenerRegistration? = null
    private var followerCountListener: ListenerRegistration? = null
    private var followingCountListener: ListenerRegistration? = null
    private var recipeListener: ListenerRegistration? = null

    fun loadAuthorProfile(authorId: String) {
        val currentUserId = auth.currentUser?.uid

        clearListeners()

        // 1. Thông tin Author
        userListener = firestore.collection("users").document(authorId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val dbUsername = doc.getString("username")
                    val email = doc.getString("email")?.substringBefore("@") ?: ""

                    _uiState.value = _uiState.value.copy(
                        uid = authorId,
                        fullName = doc.getString("fullName") ?: "Người dùng",
                        username = if (!dbUsername.isNullOrBlank()) dbUsername else email,
                        photoUrl = doc.getString("photoUrl")
                    )
                }
            }

        // 2. Trạng thái Follow (Chỉ check nếu đã đăng nhập)
        if (currentUserId != null && currentUserId != authorId) {
            followStatusListener = firestore.collection("users").document(currentUserId)
                .collection("following").document(authorId)
                .addSnapshotListener { doc, _ ->
                    _uiState.value = _uiState.value.copy(isFollowing = doc != null && doc.exists())
                }
        }

        // 3. Đếm Follower
        followerCountListener = firestore.collection("users").document(authorId)
            .collection("followers")
            .addSnapshotListener { s, _ -> if (s != null) _uiState.value = _uiState.value.copy(followerCount = s.size()) }

        // 4. Đếm Following
        followingCountListener = firestore.collection("users").document(authorId)
            .collection("following")
            .addSnapshotListener { s, _ -> if (s != null) _uiState.value = _uiState.value.copy(followingCount = s.size()) }

        // 5. Lấy danh sách Món ăn
        recipeListener = firestore.collection("recipes")
            .whereEqualTo("userId", authorId)
            .addSnapshotListener { s, _ ->
                if (s != null) {
                    val recipes = s.documents.mapNotNull { doc ->
                        try {
                            Recipe(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                imageUrl = doc.getString("imageUrl"),
                                timeCook = doc.getLong("timeCook")?.toInt() ?: 0,
                                difficulty = doc.getString("difficulty") ?: "Dễ",
                                author = Author(id = authorId, name = "", avatarUrl = null), // Dummy
                                description = "", ingredients = emptyList(), instructions = emptyList(), relatedRecipes = emptyList(), isFavorite = false
                            )
                        } catch (e: Exception) { null }
                    }
                    _uiState.value = _uiState.value.copy(
                        recipeCount = s.size(),
                        authorRecipes = recipes
                    )
                }
            }
    }

    fun toggleFollow() {
        val currentUserId = auth.currentUser?.uid ?: return
        val authorId = _uiState.value.uid
        if (authorId.isBlank() || currentUserId == authorId) return

        val currentUserRef = firestore.collection("users").document(currentUserId)
        val authorRef = firestore.collection("users").document(authorId)

        firestore.runTransaction { transaction ->
            val followingRef = currentUserRef.collection("following").document(authorId)
            val followerRef = authorRef.collection("followers").document(currentUserId)

            val isFollowing = transaction.get(followingRef).exists()
            if (isFollowing) {
                transaction.delete(followingRef)
                transaction.delete(followerRef)
            } else {
                val data = mapOf("timestamp" to FieldValue.serverTimestamp())
                transaction.set(followingRef, data)
                transaction.set(followerRef, data)
            }
        }
    }

    private fun clearListeners() {
        userListener?.remove()
        followStatusListener?.remove()
        followerCountListener?.remove()
        followingCountListener?.remove()
        recipeListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }
}