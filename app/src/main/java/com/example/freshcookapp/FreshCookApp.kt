package com.example.freshcookapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.freshcookapp.ui.component.MyBottomBar
import com.example.freshcookapp.ui.component.MyTopBar
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.ui.nav.MyAppNavgation
import com.example.freshcookapp.ui.theme.Blue
import com.example.freshcookapp.ui.theme.FreshCookAppTheme
import com.example.freshcookapp.ui.theme.Red
import com.example.freshcookapp.ui.theme.White
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.freshcookapp.auth.firebaseAuthWithGoogle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreshCookApp(auth: FirebaseAuth, googleSignInClient: GoogleSignInClient) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current

    // 1. Quyết định màn hình bắt đầu
    val startDestination = if (auth.currentUser != null) {
        Destination.Home
    } else {
        Destination.Splash
    }

    // 2. Tạo Google Sign-In Launcher (Popup)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("GoogleSignIn", "Firebase auth với Google: ${account.id}")

            // Gọi hàm helper (từ MainActivity)
            firebaseAuthWithGoogle(account.idToken!!, auth) { success, userName ->
                if (success) {
                    Toast.makeText(context, "Đăng nhập thành công: ${userName ?: ""}", Toast.LENGTH_SHORT).show()
                    // Đăng nhập thành công, điều hướng về Home
                    navController.navigate(Destination.Home) {
                        popUpTo(0) // Xóa toàn bộ lịch sử
                    }
                } else {
                    Toast.makeText(context, "Đăng nhập thất bại: $userName", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Đăng nhập Google thất bại", e)
            Toast.makeText(context, "Lỗi Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val noBottomBarDestinations = listOf(
        Destination.Splash::class.qualifiedName,
        Destination.Welcome::class.qualifiedName,
        Destination.Login::class.qualifiedName,
        Destination.Register::class.qualifiedName,
        Destination.Search::class.qualifiedName,
        Destination.Filter::class.qualifiedName,
        Destination.Notification::class.qualifiedName,
        Destination.Settings::class.qualifiedName,
        Destination.Follow::class.qualifiedName,
        Destination.RecentlyViewed::class.qualifiedName,
        Destination.MyDishes::class.qualifiedName,
        Destination.RecipeDetail::class.qualifiedName,
        "user_profile/{userId}"
    )

    // ✅ So khớp thông minh cho cả route động
    val hideBottomBar = noBottomBarDestinations.any { pattern ->
        pattern != null && currentDestination?.route?.startsWith(pattern.substringBefore("/{")) == true
    }

    Scaffold(
        containerColor = White,
        bottomBar = {
            if (!hideBottomBar) {
                MyBottomBar(navController, currentDestination)
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = White
        ) {
            MyAppNavgation(
                navController = navController,
                startDestination = startDestination, // <-- THÊM MỚI
                onGoogleSignInClick = { // <-- THÊM MỚI
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                }
            )
        }
    }
}

