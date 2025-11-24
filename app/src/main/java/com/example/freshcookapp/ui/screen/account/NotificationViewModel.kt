package com.example.freshcookapp.ui.screen.account

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // Lắng nghe vào collection: users -> {uid} -> notifications
        firestore.collection("users").document(currentUserId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Mới nhất lên đầu
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        NotificationModel(
                            id = doc.id,
                            userId = doc.getString("senderId") ?: "",
                            userName = doc.getString("senderName") ?: "Ai đó",
                            userAvatar = doc.getString("senderAvatar"),
                            message = doc.getString("message") ?: "",
                            time = convertTimestampToTime(timestamp),
                            isRead = doc.getBoolean("isRead") ?: false,
                            recipeId = doc.getString("recipeId") // ID món ăn (nếu có) để click vào
                        )
                    }
                    _notifications.value = list
                }
            }
    }

    // Hàm phụ: Đổi timestamp thành giờ (Ví dụ: "10:30 24/11")
    private fun convertTimestampToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}