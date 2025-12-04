package com.example.freshcookapp

import android.content.Intent
import android.graphics.Color // 🔥 Import màu để dùng cho SystemBarStyle
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle // 🔥 Import để chỉnh style thanh trạng thái
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.ui.theme.FreshCookAppTheme
import com.example.freshcookapp.ui.theme.ThemeViewModel
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 SỬA LỖI TRÙNG MÀU STATUS BAR TẠI ĐÂY
        // Dòng này báo cho hệ thống biết: App tôi nền sáng (light), hãy vẽ icon màu tối (dark)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, // Màu nền của thanh trạng thái (trong suốt)
                Color.TRANSPARENT  // Màu nền khi ở chế độ tối (trong suốt)
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        // -----------------------------------------------------------

        // 🔥 Khởi tạo Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        auth = Firebase.auth

        // 🔥 BẬT FIREBASE OFFLINE PERSISTENCE
        try {
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings
            Log.d("Firestore", "✅ Đã bật Offline Persistence")
        } catch (e: Exception) {
            Log.e("Firestore", "❌ Lỗi bật Offline Persistence", e)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1084160906105-mc8fh3ppnv6qf26lbgo7rb0nr30itl9a.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        updateFcmToken()

        val deepLinkRecipeId = intent.getStringExtra("recipeId")
        val deepLinkUserId = intent.getStringExtra("userId")

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            // Lưu ý: Biến mode này nên được xử lý trong Theme để đổi màu Status Bar động
            val mode by themeViewModel.themeMode.collectAsState()

            FreshCookAppTheme(themeMode = mode) {
                FreshCookApp(
                    auth = auth,
                    googleSignInClient = googleSignInClient,
                    deepLinkRecipeId = deepLinkRecipeId,
                    deepLinkUserId = deepLinkUserId
                )
            }
        }
    }

    private fun updateFcmToken() {
        val currentUser = auth.currentUser ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Lỗi lấy token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val mode by themeViewModel.themeMode.collectAsState()

            FreshCookAppTheme(themeMode = mode) {
                FreshCookApp(
                    auth = auth,
                    googleSignInClient = googleSignInClient,
                    deepLinkRecipeId = intent.getStringExtra("recipeId"),
                    deepLinkUserId = intent.getStringExtra("userId")
                )
            }
        }
    }
}