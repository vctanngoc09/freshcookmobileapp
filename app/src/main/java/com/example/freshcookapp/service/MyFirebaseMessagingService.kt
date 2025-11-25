package com.example.freshcookapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.freshcookapp.MainActivity
import com.example.freshcookapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 1. KHI CÓ TOKEN MỚI (LẦN ĐẦU CÀI APP HOẶC CÀI LẠI)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        saveTokenToFirestore(token)
    }

    // Lưu token vào document của User hiện tại
    private fun saveTokenToFirestore(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token) // Lưu vào trường fcmToken
                .addOnFailureListener { e -> Log.e("FCM", "Lỗi lưu token", e) }
        }
    }

    // 2. KHI NHẬN ĐƯỢC TIN NHẮN TỪ FIREBASE
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Lấy dữ liệu từ tin nhắn (Data Payload)
        val title = remoteMessage.notification?.title ?: "FreshCook"
        val body = remoteMessage.notification?.body ?: "Bạn có thông báo mới"

        // Dữ liệu điều hướng (Ví dụ: recipeId để mở món ăn)
        val data = remoteMessage.data
        val recipeId = data["recipeId"]
        val userId = data["userId"] // ID người dùng để mở profile

        showNotification(title, body, recipeId, userId)
    }

    private fun showNotification(title: String, body: String, recipeId: String?, userId: String?) {
        val channelId = "fresh_cook_channel"

        // Intent để mở MainActivity khi bấm vào thông báo
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Truyền dữ liệu để MainActivity biết cần mở trang nào
            putExtra("recipeId", recipeId)
            putExtra("userId", userId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications) // Đảm bảo bạn có icon này (màu trắng trong suốt)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo kênh thông báo (Bắt buộc cho Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FreshCook Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}