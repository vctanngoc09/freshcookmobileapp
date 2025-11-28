package com.example.freshcookapp.ui.screen.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.InstructionStep
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.domain.model.RecipePreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.freshcookapp.data.repository.CommentRepository
import com.example.freshcookapp.domain.model.Comment
import java.util.Date

class RecipeDetailViewModel(
    private val repository: RecipeRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    private val _isFollowingAuthor = MutableStateFlow(false)
    val isFollowingAuthor: StateFlow<Boolean> = _isFollowingAuthor

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications

    // 🔥 BIẾN MỚI: Lưu tên người đang được trả lời (Smart Reply)
    private val _replyingToUser = MutableStateFlow<String?>(null)
    val replyingToUser: StateFlow<String?> = _replyingToUser

    init {
        listenToUnreadNotifications()
    }

    private fun listenToUnreadNotifications() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(currentUserId)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    _hasUnreadNotifications.value = snapshot.size() > 0
                }
            }
    }

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val localEntity = repository.getRecipeById(recipeId)

                if (localEntity != null) {
                    repository.addToHistory(recipeId)

                    val relatedEntities = repository.getRelatedRecipes(localEntity.categoryId, localEntity.id).first()
                    val relatedList = relatedEntities.map { entity ->
                        RecipePreview(
                            id = entity.id,
                            title = entity.name,
                            time = "${entity.timeCook} phút",
                            author = "",
                            imageUrl = entity.imageUrl
                        )
                    }

                    var currentRecipe = localEntity.toUiModel(
                        Author(localEntity.userId, "Đang tải...", null),
                        relatedList,
                        localEntity.likeCount
                    )
                    _recipe.value = currentRecipe

                    viewModelScope.launch {
                        repository.getRecipeFlow(recipeId).collect { updatedEntity ->
                            if (updatedEntity != null) {
                                _recipe.value = _recipe.value?.copy(
                                    isFavorite = updatedEntity.isFavorite,
                                    likeCount = updatedEntity.likeCount
                                ) ?: updatedEntity.toUiModel(
                                    Author(updatedEntity.userId, "Đang tải...", null),
                                    relatedList,
                                    updatedEntity.likeCount
                                )
                            }
                        }
                    }

                    fetchAuthorInfo(localEntity.userId) { author ->
                        currentRecipe = currentRecipe.copy(author = author)
                        _recipe.value = currentRecipe
                        checkFollowStatus(localEntity.userId)
                    }

                    firestore.collection("recipes").document(recipeId)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null && snapshot.exists()) {
                                val liveLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0
                                _recipe.value = _recipe.value?.copy(likeCount = liveLikeCount)
                            }
                        }

                    firestore.collection("recipes").document(recipeId)
                        .collection("instruction")
                        .orderBy("step", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                val fullSteps = snapshot.documents.map { doc ->
                                    InstructionStep(
                                        stepNumber = doc.getLong("step")?.toInt() ?: 0,
                                        description = doc.getString("description") ?: "",
                                        imageUrl = doc.getString("imageUrl")
                                    )
                                }
                                _recipe.value = _recipe.value?.copy(instructions = fullSteps)
                            }
                        }

                    checkIfUserLiked(recipeId) { isLiked ->
                        _recipe.value = _recipe.value?.copy(isFavorite = isLiked)
                    }

                    viewModelScope.launch {
                        commentRepository.getCommentsForRecipe(recipeId).collectLatest { comments ->
                            _comments.value = comments
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchAuthorInfo(authorId: String, onResult: (Author) -> Unit) {
        firestore.collection("users").document(authorId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("fullName") ?: "Đầu bếp"
                val avatar = doc.getString("photoUrl")
                onResult(Author(authorId, name, avatar))
            }
            .addOnFailureListener {
                onResult(Author(authorId, "Người dùng", null))
            }
    }

    private fun checkIfUserLiked(recipeId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId).get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    fun toggleFavorite() {
        val currentRecipe = _recipe.value ?: return
        val currentUser = auth.currentUser ?: return

        val desiredState = !currentRecipe.isFavorite

        // Delegate atomic optimistic toggle to repository which handles local optimistic update and remote transaction.
        viewModelScope.launch {
            repository.toggleFavoriteWithRemote(currentUser.uid, currentRecipe.id, desiredState)

            // After repository completes (it already updated Room optimistically), send notification if liked
            if (desiredState && currentRecipe.author.id != currentUser.uid) {
                sendNotification(currentRecipe.author.id, "đã yêu thích món ăn: ${currentRecipe.name}", currentRecipe.id)
            }
        }
    }

    private fun checkFollowStatus(authorId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == authorId) return
        firestore.collection("users").document(currentUserId).collection("following").document(authorId)
            .addSnapshotListener { s, _ -> _isFollowingAuthor.value = s != null && s.exists() }
    }

    fun toggleFollowAuthor() {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentRecipe = _recipe.value ?: return
        val authorId = currentRecipe.author.id
        if (currentUserId == authorId) return

        val currentUserRef = firestore.collection("users").document(currentUserId)
        val authorRef = firestore.collection("users").document(authorId)
        val followingRef = currentUserRef.collection("following").document(authorId)
        val followerRef = authorRef.collection("followers").document(currentUserId)

        firestore.runTransaction { transaction ->
            if (transaction.get(followingRef).exists()) {
                transaction.delete(followingRef); transaction.delete(followerRef)
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
                transaction.update(authorRef, "followerCount", FieldValue.increment(-1))
                false
            } else {
                transaction.set(followingRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.set(followerRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.set(currentUserRef, mapOf("followingCount" to FieldValue.increment(1)), SetOptions.merge())
                transaction.set(authorRef, mapOf("followerCount" to FieldValue.increment(1)), SetOptions.merge())
                true
            }
        }.addOnSuccessListener { isFollowed ->
            if (isFollowed && currentUserId != authorId) {
                sendNotification(authorId, "đã bắt đầu theo dõi bạn", null)
            }
        }
    }

    private fun sendNotification(receiverId: String, message: String, recipeId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == receiverId) return

        firestore.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
            val noti = hashMapOf(
                "senderId" to currentUserId,
                "senderName" to (doc.getString("fullName") ?: "Ai đó"),
                "senderAvatar" to doc.getString("photoUrl"),
                "message" to message,
                "recipeId" to recipeId,
                "timestamp" to FieldValue.serverTimestamp(),
                "isRead" to false,
                "type" to if (recipeId != null) "like" else "follow"
            )
            firestore.collection("users").document(receiverId).collection("notifications").add(noti)
        }
    }

    // --- CÁC HÀM XỬ LÝ BÌNH LUẬN NÂNG CAO ---

    fun updateCommentText(text: String) { _commentText.value = text }

    // 1. Đặt trạng thái đang reply
    fun onReplyToComment(username: String) {
        _replyingToUser.value = username
    }

    // 2. Hủy reply
    fun onCancelReply() {
        _replyingToUser.value = null
    }

    fun addComment() {
        val rawText = _commentText.value.trim()
        if (rawText.isEmpty()) return

        val user = auth.currentUser ?: return
        val recipe = _recipe.value ?: return

        // Logic thông minh: Tự động chèn tag vào đầu nội dung nếu đang reply
        val replyPrefix = _replyingToUser.value?.let { "@$it " } ?: ""
        val finalContent = replyPrefix + rawText

        firestore.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val avatarUrl = doc.getString("photoUrl") ?: user.photoUrl?.toString()

            val comment = Comment(
                userId = user.uid,
                recipeId = recipe.id,
                userName = doc.getString("fullName") ?: "User",
                userAvatar = avatarUrl,
                text = finalContent, // Gửi nội dung đã có tag
                timestamp = Date()
            )

            viewModelScope.launch {
                if (commentRepository.addComment(comment)) {
                    _commentText.value = ""
                    _replyingToUser.value = null // Reset trạng thái reply sau khi gửi

                    if (user.uid != recipe.author.id) {
                        sendNotification(recipe.author.id, "đã bình luận: ${recipe.name}", recipe.id)
                    }
                }
            }
        }
    }

    fun toggleLikeComment(comment: Comment) {
        val currentUserId = auth.currentUser?.uid ?: return
        val isLiked = comment.likedBy.contains(currentUserId)
        val commentRef = firestore.collection("recipes").document(comment.recipeId)
            .collection("comments").document(comment.id)

        if (isLiked) {
            commentRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
        } else {
            commentRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener {
                    if (comment.userId != currentUserId) {
                        sendNotification(
                            receiverId = comment.userId,
                            message = "đã thích bình luận: \"${comment.text}\"",
                            recipeId = comment.recipeId
                        )
                    }
                }
        }
    }

    fun deleteComment(commentId: String) {
        val recipe = _recipe.value ?: return
        viewModelScope.launch { commentRepository.deleteComment(recipe.id, commentId) }
    }

    private fun RecipeEntity.toUiModel(author: Author, related: List<RecipePreview>, likes: Int): Recipe {
        return Recipe(
            id = this.id,
            name = this.name,
            timeCook = this.timeCook,
            difficulty = this.difficulty ?: "Trung bình",
            imageUrl = this.imageUrl,
            description = this.description ?: "",
            author = author,
            isFavorite = this.isFavorite,
            likeCount = likes,
            createdAt = this.createdAt,
            ingredients = this.ingredients ?: emptyList(),
            instructions = this.steps?.mapIndexed { index, s -> InstructionStep(index + 1, s, null) } ?: emptyList(),
            relatedRecipes = related
        )
    }
}