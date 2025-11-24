package com.example.freshcookapp

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.freshcookapp.ui.component.MyBottomBar
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.ui.nav.MyAppNavgation
import com.example.freshcookapp.ui.screen.auth.firebaseAuthWithGoogle
import com.example.freshcookapp.ui.theme.White
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

    val startDestination = if (auth.currentUser != null) {
        Destination.Home
    } else {
        Destination.Splash
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("GoogleSignIn", "Firebase auth with Google: ${account.id}")

            firebaseAuthWithGoogle(account.idToken!!, auth) { success, userName ->
                if (success) {
                    Toast.makeText(context, "Đăng nhập thành công: ${userName ?: ""}", Toast.LENGTH_SHORT).show()
                    navController.navigate(Destination.Home) {
                        popUpTo(0)
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
        Destination.ForgotPassword::class.qualifiedName,
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

    val hideBottomBar = noBottomBarDestinations.any { pattern ->
        pattern != null && currentDestination?.route?.startsWith(pattern.substringBefore("/{")) == true
    }

    Scaffold(
        containerColor = White,
        // 1. TẮT TỰ ĐỘNG CĂN CHỈNH (QUAN TRỌNG)
        contentWindowInsets = WindowInsets(0.dp),

        bottomBar = {
            if (!hideBottomBar) {
                MyBottomBar(navController, currentDestination)
            }
        }
    ) { innerPadding ->

        val modifier = if (hideBottomBar) {
            // Nếu ẩn BottomBar (như trang Follow):
            // Cho phép tràn toàn màn hình, trang Follow sẽ tự lo phần tai thỏ
            Modifier.fillMaxSize()
        } else {
            // Nếu hiện BottomBar (như trang Home):
            // Tự tay thêm padding cho BottomBar (innerPadding)
            // VÀ tự tay thêm padding cho StatusBar (.statusBarsPadding) -> Khắc phục lỗi che chữ
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        }

        Surface(
            modifier = modifier,
            color = White
        ) {
            MyAppNavgation(
                navController = navController,
                startDestination = startDestination,
                onGoogleSignInClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }
                }
            )
        }
    }
}