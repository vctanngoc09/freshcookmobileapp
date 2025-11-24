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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    private val _isFollowingAuthor = MutableStateFlow(false)
    val isFollowingAuthor: StateFlow<Boolean> = _isFollowingAuthor

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            // 1. Lấy dữ liệu từ Local DB (Room) trước
            val localEntity = repository.getRecipeById(recipeId)

            if (localEntity != null) {
                // Lưu lịch sử xem
                repository.addToHistory(recipeId)

                // 2. LẤY MÓN TƯƠNG TỰ (Fix lỗi mất món tương tự)
                // Lấy danh sách entity liên quan, convert sang RecipePreview
                val relatedEntities = repository.getRelatedRecipes(localEntity.categoryId, localEntity.id).first()
                val relatedList = relatedEntities.map { entity ->
                    RecipePreview(
                        id = entity.id,
                        title = entity.name,
                        time = "${entity.timeCookMinutes} phút",
                        author = "", // Preview không cần tác giả chi tiết
                        imageUrl = entity.imageUrl
                    )
                }

                // 3. Tạo UI Model ban đầu (Like count tạm để 0)
                var currentRecipe = localEntity.toUiModel(
                    Author(localEntity.userId, "Đang tải...", null),
                    relatedList,
                    0
                )
                _recipe.value = currentRecipe

                // 4. Tải thông tin Tác giả từ Firebase
                fetchAuthorInfo(localEntity.userId) { author ->
                    currentRecipe = currentRecipe.copy(author = author)
                    _recipe.value = currentRecipe
                    // Check trạng thái Follow
                    checkFollowStatus(localEntity.userId)
                }

                // 5. Lắng nghe số Like Realtime từ Firebase
                firestore.collection("recipes").document(recipeId)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val liveLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0
                            _recipe.value = _recipe.value?.copy(likeCount = liveLikeCount)
                        }
                    }

                // 6. Kiểm tra xem mình đã Like món này chưa
                checkIfUserLiked(recipeId) { isLiked ->
                    _recipe.value = _recipe.value?.copy(isFavorite = isLiked)
                }

            } else {
                Log.e("Detail", "Không tìm thấy món trong Local DB")
            }
        }
    }

    // --- CÁC HÀM PHỤ TRỢ ---

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

    // --- XỬ LÝ LIKE (TRANSACTION + NOTIFICATION) ---
    fun toggleFavorite() {
        val currentRecipe = _recipe.value ?: return
        val currentUser = auth.currentUser ?: return

        val recipeRef = firestore.collection("recipes").document(currentRecipe.id)
        val userFavRef = firestore.collection("users").document(currentUser.uid)
            .collection("favorites").document(currentRecipe.id)

        // Cập nhật UI ngay lập tức cho mượt (Optimistic Update)
        val newStatus = !currentRecipe.isFavorite
        val newCount = if (newStatus) currentRecipe.likeCount + 1 else currentRecipe.likeCount - 1
        _recipe.value = currentRecipe.copy(isFavorite = newStatus, likeCount = newCount)

        // Gửi lên Server
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userFavRef)
            if (snapshot.exists()) {
                // Đang thích -> Bỏ thích
                transaction.delete(userFavRef)
                transaction.update(recipeRef, "likeCount", FieldValue.increment(-1))
                false // Trả về false
            } else {
                // Chưa thích -> Thích
                transaction.set(userFavRef, mapOf(
                    "addedAt" to FieldValue.serverTimestamp(),
                    "recipeId" to currentRecipe.id
                ))
                transaction.set(recipeRef, mapOf("likeCount" to FieldValue.increment(1)), SetOptions.merge())
                true // Trả về true
            }
        }.addOnSuccessListener { isLiked ->
            // 1. Cập nhật Local DB (Dùng viewModelScope.launch để tránh lỗi Suspend)
            viewModelScope.launch {
                repository.toggleFavorite(currentRecipe.id, isLiked)
            }

            // 2. GỬI THÔNG BÁO (Nếu là hành động Thích)
            if (isLiked) {
                sendNotification(
                    receiverId = currentRecipe.author.id,
                    message = "đã yêu thích món ăn: ${currentRecipe.title}",
                    recipeId = currentRecipe.id
                )
            }
        }.addOnFailureListener { e ->
            Log.e("RecipeDetail", "Lỗi Like: ${e.message}")
        }
    }

    // --- XỬ LÝ FOLLOW (TRANSACTION + NOTIFICATION) ---
    private fun checkFollowStatus(authorId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == authorId) return
        firestore.collection("users").document(currentUserId)
            .collection("following").document(authorId)
            .addSnapshotListener { snapshot, _ ->
                _isFollowingAuthor.value = snapshot != null && snapshot.exists()
            }
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
            val isFollowing = transaction.get(followingRef).exists()
            if (isFollowing) {
                transaction.delete(followingRef)
                transaction.delete(followerRef)
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
                transaction.update(authorRef, "followerCount", FieldValue.increment(-1))
                false // Unfollow
            } else {
                transaction.set(followingRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.set(followerRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.set(currentUserRef, mapOf("followingCount" to FieldValue.increment(1)), SetOptions.merge())
                transaction.set(authorRef, mapOf("followerCount" to FieldValue.increment(1)), SetOptions.merge())
                true // Follow
            }
        }.addOnSuccessListener { isFollowed ->
            // GỬI THÔNG BÁO (Nếu là hành động Follow)
            if (isFollowed) {
                sendNotification(
                    receiverId = authorId,
                    message = "đã bắt đầu theo dõi bạn",
                    recipeId = null
                )
            }
        }
    }

    // --- HÀM GỬI THÔNG BÁO CHUNG ---
    private fun sendNotification(receiverId: String, message: String, recipeId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == receiverId) return // Không tự gửi cho mình

        // Lấy thông tin người gửi (Tôi) để lưu vào thông báo
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { doc ->
                val myName = doc.getString("fullName") ?: "Ai đó"
                val myAvatar = doc.getString("photoUrl")

                val notificationData = hashMapOf(
                    "senderId" to currentUserId,
                    "senderName" to myName,
                    "senderAvatar" to myAvatar,
                    "message" to message,
                    "recipeId" to recipeId,
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false,
                    "type" to if (recipeId != null) "like" else "follow"
                )

                // Lưu vào sub-collection 'notifications' của NGƯỜI NHẬN
                firestore.collection("users").document(receiverId)
                    .collection("notifications")
                    .add(notificationData)
            }
    }

    // --- MAPPER ---
    private fun RecipeEntity.toUiModel(author: Author, related: List<RecipePreview>, likes: Int): Recipe {
        return Recipe(
            id = this.id,
            title = this.name,
            time = "${this.timeCookMinutes} phút",
            level = this.level ?: "Trung bình",
            imageUrl = this.imageUrl,
            description = this.description ?: "",
            author = author,
            isFavorite = this.isFavorite,
            likeCount = likes,
            ingredients = this.ingredients,
            instructions = this.steps.mapIndexed { index, s -> InstructionStep(index + 1, s, null) },
            relatedRecipes = related // Đã gán danh sách món tương tự vào đây
        )
    }
}