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

class RecipeDetailViewModel(private val repository: RecipeRepository, private val commentRepository: CommentRepository) : ViewModel() {

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

    // --- MỚI: TRẠNG THÁI THÔNG BÁO ---
    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications

    init {
        listenToUnreadNotifications()
    }

    // Hàm lắng nghe thông báo chưa đọc
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

                    // Observe local Room changes for this recipe so Detail updates when Home changes favorite/like
                    viewModelScope.launch {
                        repository.getRecipeFlow(recipeId).collect { updatedEntity ->
                            if (updatedEntity != null) {
                                // Only update favorite flag and likeCount from local DB to avoid overwriting author/instructions
                                _recipe.value = _recipe.value?.copy(
                                    isFavorite = updatedEntity.isFavorite,
                                    likeCount = updatedEntity.likeCount
                                ) ?: updatedEntity.toUiModel(Author(updatedEntity.userId, "Đang tải...", null), relatedList, updatedEntity.likeCount)
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

                } else {
                    Log.e("Detail", "Không tìm thấy món trong Local DB")
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
        val recipeRef = firestore.collection("recipes").document(currentRecipe.id)
        val userFavRef = firestore.collection("users").document(currentUser.uid).collection("favorites").document(currentRecipe.id)

        val newStatus = !currentRecipe.isFavorite
        val newCount = if (newStatus) currentRecipe.likeCount + 1 else currentRecipe.likeCount - 1
        _recipe.value = currentRecipe.copy(isFavorite = newStatus, likeCount = newCount)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userFavRef)
            if (snapshot.exists()) {
                transaction.delete(userFavRef)
                transaction.update(recipeRef, "likeCount", FieldValue.increment(-1))
                false
            } else {
                transaction.set(userFavRef, mapOf("addedAt" to FieldValue.serverTimestamp(), "recipeId" to currentRecipe.id))
                transaction.set(recipeRef, mapOf("likeCount" to FieldValue.increment(1)), SetOptions.merge())
                true
            }
        }.addOnSuccessListener { isLiked ->
            viewModelScope.launch {
                // Pass newCount so repository updates like_count in Room as well
                repository.toggleFavorite(currentRecipe.id, isLiked, newCount)
            }
            if (isLiked) sendNotification(currentRecipe.author.id, "đã yêu thích món ăn: ${currentRecipe.name}", currentRecipe.id)
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
            if (isFollowed) sendNotification(authorId, "đã bắt đầu theo dõi bạn", null)
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

    fun updateCommentText(text: String) { _commentText.value = text }

    fun addComment() {
        val text = _commentText.value.trim(); if (text.isEmpty()) return
        val user = auth.currentUser ?: return; val recipe = _recipe.value ?: return
        firestore.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val comment = Comment(userId = user.uid, recipeId = recipe.id, userName = doc.getString("fullName") ?: "User", text = text)
            viewModelScope.launch {
                if (commentRepository.addComment(comment)) {
                    _commentText.value = ""
                    sendNotification(recipe.author.id, "đã bình luận: ${recipe.name}", recipe.id)
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        val recipe = _recipe.value ?: return
        viewModelScope.launch { commentRepository.deleteComment(recipe.id, commentId) }
    }

    fun deleteSampleComments() { /* Logic xóa sample */ }
    fun addSampleComment() { /* Logic thêm sample */ }

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
            ingredients = this.ingredients ?: emptyList(),
            instructions = this.steps?.mapIndexed { index, s -> InstructionStep(index + 1, s, null) } ?: emptyList(),
            relatedRecipes = related
        )
    }
}