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

                    // --- SỬA: Map thêm isFavorite từ entity sang RecipePreview ---
                    val relatedList = relatedEntities.map { entity ->
                        RecipePreview(
                            id = entity.id,
                            title = entity.name,
                            time = "${entity.timeCook} phút",
                            author = "",
                            imageUrl = entity.imageUrl,
                            isFavorite = entity.isFavorite // Quan trọng
                        )
                    }

                    _recipe.value = localEntity.toUiModel(
                        Author(localEntity.userId, "Đang tải...", null),
                        relatedList,
                        localEntity.likeCount
                    )

                    // Lắng nghe thay đổi từ Room (để cập nhật real-time nếu có thay đổi ở màn hình khác)
                    viewModelScope.launch {
                        repository.getRecipeFlow(recipeId).collect { updatedEntity ->
                            if (updatedEntity != null) {
                                val currentAuthor = _recipe.value?.author ?: Author(updatedEntity.userId, "Đang tải...", null)
                                // Lưu ý: relatedList ở đây đang là static từ lần load đầu,
                                // nếu muốn related list cũng real-time favorite thì cần logic phức tạp hơn,
                                // nhưng tạm thời giữ nguyên relatedList để tránh load lại nhiều lần.
                                _recipe.value = updatedEntity.toUiModel(
                                    currentAuthor,
                                    relatedList, // Giữ danh sách cũ
                                    updatedEntity.likeCount
                                )
                            }
                        }
                    }

                    if (localEntity.userId.isNotBlank()) {
                        fetchAuthorInfo(localEntity.userId) { author ->
                            _recipe.value = _recipe.value?.copy(author = author)
                            checkFollowStatus(localEntity.userId)
                        }
                    } else {
                        Log.e("RecipeDetailVM", "Author ID from Room is blank for recipe ID: $recipeId")
                    }

                    // Listen Realtime Like Count & UserId from Firestore
                    firestore.collection("recipes").document(recipeId)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null && snapshot.exists()) {
                                val liveLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0
                                val firestoreUserId = snapshot.getString("userId")

                                _recipe.value = _recipe.value?.copy(likeCount = liveLikeCount)

                                if (!firestoreUserId.isNullOrBlank() && (_recipe.value?.author?.id.isNullOrBlank() || _recipe.value?.author?.id != firestoreUserId)) {
                                    fetchAuthorInfo(firestoreUserId) { author ->
                                        _recipe.value = _recipe.value?.copy(author = author)
                                        checkFollowStatus(firestoreUserId)
                                    }
                                }
                            }
                        }

                    // Load Instructions
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

                    // Load Ingredients
                    firestore.collection("recipes").document(recipeId)
                        .collection("recipeIngredients")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                val ingredientsList = snapshot.documents.mapNotNull { doc ->
                                    doc.getString("name")
                                }
                                _recipe.value = _recipe.value?.copy(ingredients = ingredientsList)
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
                if (doc != null && doc.exists()) {
                    val name = doc.getString("fullName") ?: "Đầu bếp"
                    val avatar = doc.getString("photoUrl")
                    onResult(Author(authorId, name, avatar))
                } else {
                    onResult(Author(authorId, "Người dùng không tồn tại", null))
                }
            }
            .addOnFailureListener {
                Log.e("RecipeDetailVM", "Failed to fetch author info")
            }
    }

    private fun checkIfUserLiked(recipeId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId).get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    // Toggle tim cho món CHÍNH
    fun toggleFavorite() {
        val currentRecipe = _recipe.value ?: return
        val currentUser = auth.currentUser ?: return
        val authorId = currentRecipe.userId ?: currentRecipe.author.id
        if (authorId.isBlank()) return

        val desiredState = !currentRecipe.isFavorite

        viewModelScope.launch {
            repository.toggleFavoriteWithRemote(currentUser.uid, currentRecipe.id, desiredState)

            if (desiredState) {
                sendNotification(authorId, "đã yêu thích món ăn: ${currentRecipe.name}", currentRecipe.id)
            }
        }
    }

    // --- MỚI: Toggle tim cho món TƯƠNG TỰ ---
    fun toggleRelatedFavorite(targetId: String) {
        val currentRecipe = _recipe.value ?: return
        val currentUser = auth.currentUser ?: return

        // 1. Tìm item trong list
        val targetItem = currentRecipe.relatedRecipes.find { it.id == targetId } ?: return
        val newStatus = !targetItem.isFavorite

        // 2. Cập nhật UI ngay lập tức (Optimistic Update)
        val updatedRelatedList = currentRecipe.relatedRecipes.map { item ->
            if (item.id == targetId) item.copy(isFavorite = newStatus) else item
        }
        _recipe.value = currentRecipe.copy(relatedRecipes = updatedRelatedList)

        // 3. Gọi xuống Repository để đồng bộ DB & Firebase
        viewModelScope.launch {
            repository.toggleFavoriteWithRemote(currentUser.uid, targetId, newStatus)
            // Có thể gửi thông báo nếu muốn (logic tương tự toggleFavorite)
        }
    }

    private fun checkFollowStatus(authorId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == authorId || authorId.isBlank()) {
            _isFollowingAuthor.value = false
            return
        }
        firestore.collection("users").document(currentUserId).collection("following").document(authorId)
            .addSnapshotListener { s, _ -> _isFollowingAuthor.value = s != null && s.exists() }
    }

    fun toggleFollowAuthor() {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentRecipe = _recipe.value ?: return
        val authorId = currentRecipe.userId ?: currentRecipe.author.id

        if (currentUserId == authorId || authorId.isBlank()) return

        val currentUserRef = firestore.collection("users").document(currentUserId)
        val authorRef = firestore.collection("users").document(authorId)
        val followingRef = currentUserRef.collection("following").document(authorId)
        val followerRef = authorRef.collection("followers").document(currentUserId)

        firestore.runTransaction { transaction ->
            val followingDoc = transaction.get(followingRef)
            if (followingDoc.exists()) {
                transaction.delete(followingRef)
                transaction.delete(followerRef)
            } else {
                transaction.set(followingRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.set(followerRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
            }
        }.addOnSuccessListener {
            if (!(_isFollowingAuthor.value)) {
                sendNotification(authorId, "đã bắt đầu theo dõi bạn", null)
            }
        }.addOnFailureListener { e ->
            Log.e("RecipeDetailVM", "Follow/unfollow transaction FAILED", e)
        }
    }

    // --- SỬA: Dùng key "targetId" để khớp với NotificationScreen ---
    private fun sendNotification(receiverId: String, message: String, recipeId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == receiverId || receiverId.isBlank()) return

        firestore.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
            val noti = hashMapOf(
                "senderId" to currentUserId,
                "senderName" to (doc.getString("fullName") ?: "Ai đó"),
                "senderAvatar" to doc.getString("photoUrl"),
                "message" to message,
                "targetId" to recipeId, // <-- ĐỔI TỪ "recipeId" THÀNH "targetId"
                "timestamp" to FieldValue.serverTimestamp(),
                "isRead" to false,
                "type" to if (recipeId != null) "like" else "follow"
            )
            firestore.collection("users").document(receiverId).collection("notifications").add(noti)
        }
    }

    fun updateCommentText(text: String) { _commentText.value = text }

    fun onReplyToComment(username: String) { _replyingToUser.value = username }

    fun onCancelReply() { _replyingToUser.value = null }

    fun addComment() {
        val rawText = _commentText.value.trim()
        if (rawText.isEmpty()) return

        val user = auth.currentUser ?: return
        val recipe = _recipe.value ?: return
        val authorId = recipe.userId ?: recipe.author.id
        if (authorId.isBlank()) return

        val replyPrefix = _replyingToUser.value?.let { "@$it " } ?: ""
        val finalContent = replyPrefix + rawText

        firestore.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val avatarUrl = doc.getString("photoUrl") ?: user.photoUrl?.toString()

            val comment = Comment(
                userId = user.uid,
                recipeId = recipe.id,
                userName = doc.getString("fullName") ?: "User",
                userAvatar = avatarUrl,
                text = finalContent,
                timestamp = Date()
            )

            viewModelScope.launch {
                if (commentRepository.addComment(comment)) {
                    _commentText.value = ""
                    _replyingToUser.value = null

                    sendNotification(authorId, "đã bình luận: ${recipe.name}", recipe.id)
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
            relatedRecipes = related,
            userId = this.userId
        )
    }
}