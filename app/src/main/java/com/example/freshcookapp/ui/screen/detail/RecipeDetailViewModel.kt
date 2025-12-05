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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
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

    // Keep track of which recipeId we're listening comments for and cancel previous listener when switching
    private var commentsListenerRecipeId: String? = null
    private var commentsJob: Job? = null

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
                // Always start listening to comments for this recipe to support cases
                // where the recipe may not exist locally in Room yet (e.g., opened from a deep link).
                // If we're already listening to this recipe's comments, don't start another collector
                if (commentsListenerRecipeId != recipeId) {
                    // cancel any previous listener
                    commentsJob?.cancel()
                    commentsListenerRecipeId = recipeId
                    commentsJob = viewModelScope.launch {
                        commentRepository.getCommentsForRecipe(recipeId).collect { list ->
                            _comments.value = list
                        }
                    }
                }
                // 1. Load Local (Hiển thị ngay lập tức)
                val localEntity = repository.getRecipeById(recipeId)

                if (localEntity != null) {
                    repository.addToRecentlyViewed(recipeId)
                    val relatedEntities = repository.getRelatedRecipes(localEntity.categoryId, localEntity.id).first()
                    val relatedList = relatedEntities.map { entity ->
                        RecipePreview(
                            id = entity.id,
                            title = entity.name,
                            time = "${entity.timeCook} phút",
                            author = "",
                            imageUrl = entity.imageUrl,
                            isFavorite = entity.isFavorite
                        )
                    }

                    _recipe.value = localEntity.toUiModel(
                        Author(localEntity.userId, "Đang tải...", null),
                        relatedList,
                        localEntity.likeCount
                    )

                    // 2. Lắng nghe thay đổi từ Room (Local)
                    // 🔥 QUAN TRỌNG: Đã sửa logic ghi đè dữ liệu tại đây
                    viewModelScope.launch {
                        repository.getRecipeFlow(recipeId).collect { updatedEntity ->
                            if (updatedEntity != null) {
                                val currentAuthor = _recipe.value?.author ?: Author(updatedEntity.userId, "Đang tải...", null)

                                // 🔥 GIỮ LẠI DỮ LIỆU ĐÃ TẢI TỪ FIREBASE
                                val currentVideoUrl = _recipe.value?.videoUrl
                                val currentIngredients = _recipe.value?.ingredients ?: emptyList()
                                val currentInstructions = _recipe.value?.instructions ?: emptyList()

                                // Chỉ cập nhật những thứ Local quản lý (Tim, Tên, Ảnh chính...), giữ nguyên chi tiết
                                val tempRecipe = updatedEntity.toUiModel(
                                    currentAuthor,
                                    relatedList,
                                    updatedEntity.likeCount
                                )

                                // Nếu đã có dữ liệu chi tiết từ Firebase, hãy giữ lại nó!
                                _recipe.value = tempRecipe.copy(
                                    videoUrl = currentVideoUrl,
                                    ingredients = if (currentIngredients.isNotEmpty()) currentIngredients else tempRecipe.ingredients,
                                    instructions = if (currentInstructions.isNotEmpty()) currentInstructions else tempRecipe.instructions
                                )
                            }
                        }
                    }

                    if (localEntity.userId.isNotBlank()) {
                        fetchAuthorInfo(localEntity.userId) { author ->
                            _recipe.value = _recipe.value?.copy(author = author)
                            checkFollowStatus(localEntity.userId)
                        }
                    }

                    // 3. Load Video & Likes Realtime
                    firestore.collection("recipes").document(recipeId)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null && snapshot.exists()) {
                                val liveLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0
                                val firestoreUserId = snapshot.getString("userId")
                                val liveVideoUrl = snapshot.getString("videoUrl")

                                _recipe.value = _recipe.value?.copy(
                                    likeCount = liveLikeCount,
                                    videoUrl = liveVideoUrl
                                )

                                if (!firestoreUserId.isNullOrBlank() && (_recipe.value?.author?.id.isNullOrBlank() || _recipe.value?.author?.id != firestoreUserId)) {
                                    fetchAuthorInfo(firestoreUserId) { author ->
                                        _recipe.value = _recipe.value?.copy(author = author)
                                        checkFollowStatus(firestoreUserId)
                                    }
                                }
                            }
                        }

                    // 4. Load Instructions (List ảnh) - CÓ GẮN LOG DEBUG
                    firestore.collection("recipes").document(recipeId)
                        .collection("instruction")
                        .orderBy("step", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            Log.d("RecipeDebug", "================= BẮT ĐẦU LOAD BƯỚC LÀM =================")
                            Log.d("RecipeDebug", "Recipe ID: $recipeId")

                            if (!snapshot.isEmpty) {
                                Log.d("RecipeDebug", "Tìm thấy ${snapshot.size()} bước làm.")

                                val fullSteps = snapshot.documents.mapIndexed { index, doc ->
                                    val stepNum = doc.getLong("step")?.toInt() ?: (index + 1)
                                    Log.d("RecipeDebug", "--- Đang xử lý Bước $stepNum (Doc ID: ${doc.id}) ---")

                                    // 1. Kiểm tra ảnh đơn (imageUrl)
                                    val singleImage = doc.getString("imageUrl")
                                    Log.d("RecipeDebug", "   + Ảnh đơn (imageUrl): $singleImage")

                                    // 2. Kiểm tra danh sách ảnh (imageUrls) lấy trực tiếp từ Firestore
                                    val rawImageUrls = doc.get("imageUrls")
                                    Log.d("RecipeDebug", "   + Dữ liệu thô 'imageUrls' từ Firestore: Kiểu=${rawImageUrls?.javaClass?.simpleName}, Giá trị=$rawImageUrls")

                                    // 3. Ép kiểu an toàn sang List<String>
                                    val imgUrlsList = if (rawImageUrls is List<*>) {
                                        // Lọc chỉ lấy những phần tử là String và không rỗng
                                        rawImageUrls.filterIsInstance<String>().filter { it.isNotBlank() }
                                    } else {
                                        Log.w("RecipeDebug", "   ! CẢNH BÁO: 'imageUrls' không phải là List hoặc bị null.")
                                        emptyList()
                                    }

                                    Log.d("RecipeDebug", "   -> Danh sách ảnh sau khi xử lý (List<String>): $imgUrlsList (Số lượng: ${imgUrlsList.size})")

                                    InstructionStep(
                                        stepNumber = stepNum,
                                        description = doc.getString("description") ?: "",
                                        imageUrl = singleImage, // Ảnh đại diện bước
                                        imageUrls = imgUrlsList // List ảnh phụ
                                    )
                                }
                                _recipe.value = _recipe.value?.copy(instructions = fullSteps)
                                Log.d("RecipeDebug", "Đã cập nhật ${fullSteps.size} bước vào ViewModel.")
                            } else {
                                Log.w("RecipeDebug", "Không tìm thấy bước làm nào (Collection 'instruction' rỗng).")
                            }
                            Log.d("RecipeDebug", "================= KẾT THÚC LOAD BƯỚC LÀM =================")
                        }
                        .addOnFailureListener { e ->
                            Log.e("RecipeDebug", "LỖI khi tải các bước làm: ${e.message}", e)
                        }
                    // Load Ingredients
                    firestore.collection("recipes").document(recipeId)
                        .collection("recipeIngredients")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                val ingredientsList = snapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("name") ?: ""
                                    val quantity = doc.getString("quantity") ?: ""
                                    val unit = doc.getString("unit") ?: ""
                                    val note = doc.getString("note") ?: ""

                                    // Logic ghép chuỗi: "200 g Thịt bò (thái lát)"
                                    var fullString = name
                                    if (quantity.isNotBlank()) {
                                        fullString = "$quantity $unit $fullString"
                                    }
                                    if (note.isNotBlank()) {
                                        fullString = "$fullString ($note)"
                                    }
                                    fullString.trim()
                                }
                                _recipe.value = _recipe.value?.copy(ingredients = ingredientsList)
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
            .addOnFailureListener { Log.e("RecipeDetailVM", "Failed to fetch author info") }
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
        val authorId = currentRecipe.userId ?: currentRecipe.author.id
        if (authorId.isBlank()) return
        val desiredState = !currentRecipe.isFavorite
        viewModelScope.launch {
            repository.toggleFavoriteWithRemote(currentUser.uid, currentRecipe.id, desiredState)
            if (desiredState) sendNotification(authorId, "đã yêu thích món ăn: ${currentRecipe.name}", currentRecipe.id)
        }
    }

    fun toggleRelatedFavorite(targetId: String) {
        val currentRecipe = _recipe.value ?: return
        val currentUser = auth.currentUser ?: return
        val targetItem = currentRecipe.relatedRecipes.find { it.id == targetId } ?: return
        val newStatus = !targetItem.isFavorite
        val updatedRelatedList = currentRecipe.relatedRecipes.map { item ->
            if (item.id == targetId) item.copy(isFavorite = newStatus) else item
        }
        _recipe.value = currentRecipe.copy(relatedRecipes = updatedRelatedList)
        viewModelScope.launch {
            repository.toggleFavoriteWithRemote(currentUser.uid, targetId, newStatus)
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
                transaction.delete(followingRef); transaction.delete(followerRef)
            } else {
                transaction.set(followingRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.set(followerRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
            }
        }.addOnSuccessListener {
            if (!(_isFollowingAuthor.value)) sendNotification(authorId, "đã bắt đầu theo dõi bạn", null)
        }.addOnFailureListener { e -> Log.e("RecipeDetailVM", "Follow/unfollow transaction FAILED", e) }
    }

    private fun sendNotification(receiverId: String, message: String, recipeId: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == receiverId || receiverId.isBlank()) return
        firestore.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
            val noti = hashMapOf(
                "senderId" to currentUserId,
                "senderName" to (doc.getString("fullName") ?: "Ai đó"),
                "senderAvatar" to doc.getString("photoUrl"),
                "message" to message,
                "targetId" to recipeId,
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
        // Use currently loaded recipe id if available, otherwise fallback to the recipeId we are listening to
        val targetRecipeId = _recipe.value?.id ?: commentsListenerRecipeId ?: return
        val replyPrefix = _replyingToUser.value?.let { "@${it} " } ?: ""
        val finalContent = replyPrefix + rawText
        Log.d("RecipeDetailVM", "Adding comment: recipe=$targetRecipeId user=${user.uid} text=$finalContent")

        // Get user profile info for display name/avatar
        firestore.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val avatarUrl = doc.getString("photoUrl") ?: user.photoUrl?.toString()
            val userName = doc.getString("fullName") ?: "User"
            val comment = Comment(userId = user.uid, recipeId = targetRecipeId, userName = userName, userAvatar = avatarUrl, text = finalContent, timestamp = Date())

            viewModelScope.launch {
                val ok = commentRepository.addComment(comment)
                Log.d("RecipeDetailVM", "addComment result=$ok")
                if (ok) {
                    // clear input immediately so UI feels responsive
                    _commentText.value = ""
                    _replyingToUser.value = null

                    // Try to fetch recipe authorId to send notification (best-effort)
                    firestore.collection("recipes").document(targetRecipeId).get().addOnSuccessListener { recipeDoc ->
                        val fetchedAuthorId = recipeDoc.getString("userId")
                        if (!fetchedAuthorId.isNullOrBlank()) {
                            // avoid notifying self
                            if (fetchedAuthorId != user.uid) sendNotification(fetchedAuthorId, "đã bình luận: ${recipeDoc.getString("name") ?: "món ăn"}", targetRecipeId)
                        }
                    }
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
            id = this.id, name = this.name, timeCook = this.timeCook, difficulty = this.difficulty ?: "Trung bình",
            imageUrl = this.imageUrl, description = this.description ?: "", author = author, isFavorite = this.isFavorite,
            likeCount = likes, createdAt = this.createdAt, ingredients = this.ingredients ?: emptyList(),
            instructions = this.steps?.mapIndexed { index, s -> InstructionStep(index + 1, s, null) } ?: emptyList(),
            relatedRecipes = related, userId = this.userId
        )
    }
}