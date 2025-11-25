package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// MODEL DỮ LIỆU CHÍNH
data class NotificationModel(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val message: String = "",
    val time: String = "",
    val isRead: Boolean = false,
    val recipeId: String? = null
)

class NotificationViewModel() : ViewModel() {
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

                        // Fix 1: Xử lý TIMESTAMP linh hoạt để tránh lỗi runtime (như đã bàn)
                        val timestampValue = doc.get("timestamp")
                        val timestampMillis = when (timestampValue) {
                            is Timestamp -> timestampValue.toDate().time // Fix 2: Đã có Timestamp và toDate()
                            is Long -> timestampValue
                            else -> System.currentTimeMillis()
                        }

                        val model = NotificationModel(
                            userId = doc.getString("senderId") ?: "",
                            userName = doc.getString("senderName") ?: "Ai đó",
                            userAvatar = doc.getString("senderAvatar"),
                            message = doc.getString("message") ?: "",
                            isRead = doc.getBoolean("isRead") ?: false,
                            recipeId = doc.getString("recipeId")
                        )

                        model.copy(
                            id = doc.id,
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
}