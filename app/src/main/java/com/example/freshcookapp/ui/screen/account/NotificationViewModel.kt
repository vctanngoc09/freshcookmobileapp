package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Model giữ nguyên
data class NotificationModel(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val message: String = "",
    val time: String = "",
    val isRead: Boolean = false,
    val type: String = "unknown",
    val targetId: String? = null
)

class NotificationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    init {
        listenToNotifications()
    }

    private fun listenToNotifications() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(currentUserId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("NotificationCrash", "Lỗi Listener: ${e.message}", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        // 1. Lấy dữ liệu thô từ Firebase
                        val type = doc.getString("type") ?: "unknown"
                        val senderId = doc.getString("senderId")
                        val recipeId = doc.getString("recipeId")
                        val rawTargetId = doc.getString("targetId") // Có thể null như trong ảnh

                        // 2. LOGIC TỰ ĐỘNG XÁC ĐỊNH ID ĐÍCH (SỬA Ở ĐÂY)
                        // Nếu 'targetId' có sẵn thì dùng, nếu không thì tự suy luận:
                        // - Nếu là 'follow' -> Đích đến là trang cá nhân của người gửi (senderId)
                        // - Nếu là 'like/comment' -> Đích đến là bài viết (recipeId)
                        val finalTargetId = if (!rawTargetId.isNullOrEmpty()) {
                            rawTargetId
                        } else {
                            when (type) {
                                "follow" -> senderId
                                "like", "comment" -> recipeId
                                else -> null
                            }
                        }

                        val timestampValue = doc.get("timestamp")
                        val timestampMillis = when (timestampValue) {
                            is Timestamp -> timestampValue.toDate().time
                            is Long -> timestampValue
                            else -> System.currentTimeMillis()
                        }

                        val model = NotificationModel(
                            id = doc.id,
                            userId = senderId ?: "",
                            userName = doc.getString("senderName") ?: "Ai đó",
                            userAvatar = doc.getString("senderAvatar"),
                            message = doc.getString("message") ?: "",
                            isRead = doc.getBoolean("isRead") ?: false,
                            type = type,
                            targetId = finalTargetId // Gán ID đã xử lý vào đây
                        )

                        model.copy(
                            time = convertTimestampToTime(timestampMillis)
                        )
                    }
                    _notifications.value = list
                }
            }
    }

    private fun convertTimestampToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun markAllAsRead() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestore.collection("users").document(currentUserId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) return@addOnSuccessListener

                    val batch = firestore.batch()
                    snapshot.documents.forEach { doc ->
                        val notificationRef = firestore.collection("users").document(currentUserId)
                            .collection("notifications").document(doc.id)
                        batch.update(notificationRef, "isRead", true)
                    }
                    batch.commit()
                }
        }
    }

    fun deleteNotification(notificationId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(currentUserId)
            .collection("notifications").document(notificationId)
            .delete()
    }
}