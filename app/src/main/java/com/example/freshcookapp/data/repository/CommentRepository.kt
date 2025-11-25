package com.example.freshcookapp.data.repository

import android.util.Log
import com.example.freshcookapp.domain.model.Comment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getCommentsForRecipe(recipeId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("recipes").document(recipeId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log lỗi và emit empty list thay vì crash
                    Log.e("CommentRepository", "Error loading comments: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        // Đọc document và gán id từ doc.id để đảm bảo id không rỗng trong model
                        doc.toObject(Comment::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("CommentRepository", "Error parsing comment: ${e.message}")
                        null // Skip invalid comment
                    }
                } ?: emptyList()

                trySend(comments)
            }

        awaitClose { listener.remove() }
    }

    suspend fun addComment(comment: Comment): Boolean {
        return try {
            val commentsRef = firestore.collection("recipes").document(comment.recipeId).collection("comments")
            // Tạo document ref trước để có id và lưu luôn trường id trong document (tránh lưu id = "")
            val newDocRef = commentsRef.document()

            val data = mapOf(
                "id" to newDocRef.id,
                "userId" to comment.userId,
                "recipeId" to comment.recipeId,
                "userName" to comment.userName,
                "text" to comment.text,
                "timestamp" to FieldValue.serverTimestamp()
            )

            newDocRef.set(data).await()
            true
        } catch (e: Exception) {
            Log.e("CommentRepository", "Error adding comment: ${e.message}")
            false
        }
    }

    suspend fun addSampleComment(recipeId: String): Boolean {
        return try {
            val commentsRef = firestore.collection("recipes").document(recipeId).collection("comments")
            val newDocRef = commentsRef.document()

            val sampleData = mapOf(
                "id" to newDocRef.id,
                "userId" to "sampleUserId",
                "recipeId" to recipeId,
                "userName" to "Người dùng mẫu",
                "text" to "Đây là comment mẫu để test realtime!",
                "timestamp" to FieldValue.serverTimestamp()
            )

            newDocRef.set(sampleData).await()
            true
        } catch (e: Exception) {
            Log.e("CommentRepository", "Error adding sample comment: ${e.message}")
            false
        }
    }
}
