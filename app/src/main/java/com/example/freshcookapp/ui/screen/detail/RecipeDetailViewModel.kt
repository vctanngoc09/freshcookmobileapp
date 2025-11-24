package com.example.freshcookapp.ui.screen.detail

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    // Trạng thái: Mình có đang follow tác giả này không?
    private val _isFollowingAuthor = MutableStateFlow(false)
    val isFollowingAuthor: StateFlow<Boolean> = _isFollowingAuthor

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            val entity = repository.getRecipeById(recipeId) ?: return@launch
            repository.addToHistory(recipeId)

            // 1. Lấy thông tin tác giả
            val author = fetchRealAuthorFromFirebase(entity.userId)

            // 2. Kiểm tra ngay xem mình đã follow người này chưa
            checkFollowStatus(entity.userId)

            // 3. Lấy món tương tự
            // (Nếu database chưa có cột categoryId, tạm comment dòng này để tránh lỗi)
            val relatedEntities = repository.getRelatedRecipes(entity.categoryId, entity.id).first()
            val relatedRecipes = relatedEntities.map {
                RecipePreview(it.id, it.name, "", "", it.imageUrl)
            }

            _recipe.value = entity.toUiModel(author, relatedRecipes)
        }
    }

    // Logic kiểm tra Follow (Lắng nghe Realtime)
    private fun checkFollowStatus(authorId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (currentUserId == authorId) return // Không cần check nếu là chính mình

        firestore.collection("users").document(currentUserId)
            .collection("following").document(authorId)
            .addSnapshotListener { snapshot, _ ->
                _isFollowingAuthor.value = snapshot != null && snapshot.exists()
            }
    }

    // Logic thực hiện Follow/Unfollow (Copy từ Profile qua)
    fun toggleFollowAuthor() {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentRecipe = _recipe.value ?: return
        val authorId = currentRecipe.author.id

        if (currentUserId == authorId) return

        val currentUserRef = firestore.collection("users").document(currentUserId)
        val authorRef = firestore.collection("users").document(authorId)
        val followingRef = currentUserRef.collection("following").document(authorId)
        val followerRef = authorRef.collection("followers").document(currentUserId)

        viewModelScope.launch {
            firestore.runTransaction { transaction ->
                val isFollowing = transaction.get(followingRef).exists()
                if (isFollowing) {
                    // Unfollow
                    transaction.delete(followingRef)
                    transaction.delete(followerRef)
                    transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
                    transaction.update(authorRef, "followerCount", FieldValue.increment(-1))
                } else {
                    // Follow
                    transaction.set(followingRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                    transaction.set(followerRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                    transaction.update(currentUserRef, "followingCount", FieldValue.increment(1))
                    transaction.update(authorRef, "followerCount", FieldValue.increment(1))
                }
            }
        }
    }

    private suspend fun fetchRealAuthorFromFirebase(userId: String): Author {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val name = doc.getString("fullName") ?: "Người dùng ẩn danh"
            val avatar = doc.getString("photoUrl")
            Author(userId, name, avatar)
        } catch (e: Exception) {
            Author(userId, "Lỗi tải tên", null)
        }
    }

    fun toggleFavorite() {
        val currentRecipe = _recipe.value ?: return
        val newStatus = !currentRecipe.isFavorite
        val currentUser = auth.currentUser

        viewModelScope.launch {
            // 1. Cập nhật Local (Để hiển thị ngay lập tức cho mượt)
            repository.toggleFavorite(currentRecipe.id, newStatus)
            _recipe.value = currentRecipe.copy(isFavorite = newStatus)

            // 2. Cập nhật lên Firebase (Dữ liệu thực)
            if (currentUser != null) {
                updateFavoriteToFirebase(currentUser.uid, currentRecipe.id, newStatus)
            }
        }
    }

    // Hàm mới: Gửi dữ liệu lên Firebase
    private fun updateFavoriteToFirebase(userId: String, recipeId: String, isFavorite: Boolean) {
        val favoriteRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(recipeId)

        if (isFavorite) {
            // Nếu tim -> Lưu ID món ăn và thời gian
            val data = mapOf(
                "recipeId" to recipeId,
                "timestamp" to FieldValue.serverTimestamp()
            )
            favoriteRef.set(data)
        } else {
            // Nếu bỏ tim -> Xóa khỏi Firebase
            favoriteRef.delete()
        }
    }

    private fun RecipeEntity.toUiModel(realAuthor: Author, realRelated: List<RecipePreview>): Recipe {
        return Recipe(
            id = this.id,
            title = this.name,
            time = "${this.timeCookMinutes} phút",
            level = "Trung bình",
            imageRes = null,
            imageUrl = this.imageUrl,
            description = this.description ?: "",
            author = realAuthor,
            isFavorite = this.isFavorite,
            ingredients = this.ingredients,
            instructions = this.steps.mapIndexed { index, desc -> InstructionStep(index + 1, desc, null) },
            hashtags = listOf("#Ngon", "#FreshCook"),
            relatedRecipes = realRelated
        )
    }
}