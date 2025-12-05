package com.example.freshcookapp.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun Welcome(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onGithubSignInClick: () -> Unit,
    onPhoneSignInClick: () -> Unit, // Giữ tham số này để không lỗi gọi hàm bên ngoài
    onFacebookSignInClick: () -> Unit
) {
    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White) // 🔥 QUAN TRỌNG: Đặt nền màu trắng ở đây
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(250.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FRESHCOOK",
                color = Cinnabar500,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton("Đăng nhập", onClick = onLoginClick)

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton("Đăng ký", onClick = onRegisterClick)

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Hoặc tiếp tục với", color = Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))

            // --- CỤM NÚT SOCIAL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Căn giữa các nút
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Google
                IconButton(onClick = onGoogleSignInClick, modifier = Modifier.size(50.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 2. Facebook
                IconButton(onClick = onFacebookSignInClick, modifier = Modifier.size(50.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ic_face),
                        contentDescription = "Facebook",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 3. GitHub
                IconButton(onClick = onGithubSignInClick, modifier = Modifier.size(50.dp)) {
                    // Dùng ic_github nếu có, hoặc dùng tạm ic_launcher_foreground để test
                    Image(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = "Github",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}