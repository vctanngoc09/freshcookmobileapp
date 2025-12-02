package com.example.freshcookapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
        enableEdgeToEdge()

        // 🔥 Khởi tạo Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        auth = Firebase.auth

        // 🔥 BẬT FIREBASE OFFLINE PERSISTENCE - LƯU TIN NHẮN VĨNH VIỄN
        try {
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Bật lưu offline
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // Không giới hạn cache
                .build()
            firestore.firestoreSettings = settings
            Log.d("Firestore", "✅ Đã bật Offline Persistence - Tin nhắn sẽ được lưu vĩnh viễn")
        } catch (e: Exception) {
            Log.e("Firestore", "❌ Lỗi bật Offline Persistence", e)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1084160906105-mc8fh3ppnv6qf26lbgo7rb0nr30itl9a.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        updateFcmToken()

        // --- LẤY DỮ LIỆU TỪ THÔNG BÁO ---
        val deepLinkRecipeId = intent.getStringExtra("recipeId")
        val deepLinkUserId = intent.getStringExtra("userId")
        // --------------------------------

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
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

    // Xử lý khi app đang chạy mà bấm thông báo (cập nhật lại UI)
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