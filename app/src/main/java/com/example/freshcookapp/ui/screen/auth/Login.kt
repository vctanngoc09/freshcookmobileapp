package com.example.freshcookapp.ui.screen.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Black
import com.example.freshcookapp.ui.theme.Cinnabar500
import androidx.compose.foundation.Image

@Composable
fun Login(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPassClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Rất vui được gặp lại bạn!",
                style = MaterialTheme.typography.titleLarge.copy(color = Cinnabar500),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Hãy cùng quay trở lại nhé!",
                style = MaterialTheme.typography.bodyMedium,
                color = Black,
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Email
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Email",
                style = MaterialTheme.typography.bodyMedium,
                color = Black,
                textAlign = TextAlign.Left
            )
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "example@gmail.com"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Mật khẩu",
                style = MaterialTheme.typography.bodyMedium,
                color = Black,
                textAlign = TextAlign.Left
            )
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••",
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    color = Cinnabar500,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onForgotPassClick)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            PrimaryButton(
                text = "Đăng nhập",
                onClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Hoặc")

            Spacer(modifier = Modifier.height(18.dp))

            // Social Login
            Row(horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = {}) {
                    Image(painterResource(R.drawable.ic_facebook_logo), contentDescription = "Facebook")
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onGoogleSignInClick) {
                    Image(painterResource(R.drawable.ic_google_logo), contentDescription = "Google")
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = {}) {
                    Image(painterResource(R.drawable.ic_apple_logo), contentDescription = "Apple")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Text(text = "Chưa có tài khoản?")
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đăng ký",
                    color = Cinnabar500,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onRegisterClick)
                )
            }
        }
    }
}