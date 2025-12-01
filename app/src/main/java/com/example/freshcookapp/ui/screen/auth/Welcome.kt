package com.example.freshcookapp.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
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
    onPhoneSignInClick: () -> Unit,
    onFacebookSignInClick: () -> Unit   // Thêm Facebook
) {
    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google
                IconButton(onClick = onGoogleSignInClick, modifier = Modifier.size(50.dp)) {
                    Image(painterResource(R.drawable.ic_google_logo), contentDescription = "Google", modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Facebook
                IconButton(onClick = onFacebookSignInClick, modifier = Modifier.size(50.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Facebook",
                        tint = Color(0xFF1877F2), // Màu xanh Facebook
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // GitHub (Cần thêm ảnh ic_github vào drawable, nếu chưa có thì dùng tạm icon khác)
                IconButton(onClick = onGithubSignInClick, modifier = Modifier.size(50.dp)) {
                    // Bạn hãy thay R.drawable.ic_github bằng icon thực tế
                    // Nếu chưa có, dùng tạm ic_launcher_foreground để test
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Github",
                        tint = Color.Black,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Phone (Dùng Icon có sẵn của Android)
                IconButton(onClick = onPhoneSignInClick, modifier = Modifier.size(50.dp)) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Cinnabar500, // Màu đỏ chủ đạo
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    )
                }
            }
        }
    }
}