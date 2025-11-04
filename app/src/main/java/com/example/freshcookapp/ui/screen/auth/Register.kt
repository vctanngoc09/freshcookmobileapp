package com.example.freshcookapp.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
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

@Composable
fun Register(
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    ScreenContainer {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()               // 👈 tự nâng giao diện lên khi bàn phím hiện
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Back button + Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Icon lệch trái
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Black
                        )
                    }

                    // Text nằm giữa
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Cinnabar500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Full name
                Text(
                    text = "Họ và tên",
                    modifier = Modifier.fillMaxWidth(),
                    color = Black
                )
                CustomTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Nhập họ và tên"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone
                Text(
                    text = "Số điện thoại",
                    modifier = Modifier.fillMaxWidth(),
                    color = Black
                )
                CustomTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "Nhập số điện thoại"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                Text(
                    text = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    color = Black
                )
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "example@gmail.com"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                Text(
                    text = "Mật khẩu",
                    modifier = Modifier.fillMaxWidth(),
                    color = Black
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

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Password
                Text(
                    text = "Nhập lại mật khẩu",
                    modifier = Modifier.fillMaxWidth(),
                    color = Black
                )
                CustomTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "••••••",
                    visualTransformation =
                        if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible)
                                    Icons.Filled.VisibilityOff
                                else
                                    Icons.Filled.Visibility,
                                contentDescription = "Toggle password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Tạo tài khoản",
                    onClick = onRegisterClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(text = "Hoặc")

                Spacer(modifier = Modifier.height(18.dp))

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
                    Text(text = "Đã có tài khoản?")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Đăng nhập",
                        color = Cinnabar500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onLoginClick)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}