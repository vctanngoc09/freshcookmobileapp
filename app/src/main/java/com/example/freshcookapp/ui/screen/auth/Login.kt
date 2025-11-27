package com.example.freshcookapp.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
// Xóa import Black vì ta sẽ dùng màu động của Theme
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Login(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPassClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    ScreenContainer {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Thêm padding ngang ở đây để nội dung không dính sát lề màn hình
            contentPadding = PaddingValues(bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // HEADER: Nút Back
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.offset(x = (-12).dp) // Căn chỉnh lại icon cho thẳng lề
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            // SỬA: Dùng onSurface để tự động đổi màu theo nền
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // TEXT CHÀO MỪNG
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Rất vui được gặp lại bạn!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Cinnabar500, // Màu thương hiệu giữ nguyên
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Hãy cùng quay trở lại nhé!",
                    style = MaterialTheme.typography.bodyLarge,
                    // SỬA: Dùng onSurface để tự động đổi màu (Đen/Trắng)
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Left
                )

                Spacer(modifier = Modifier.height(40.dp))

                // INPUT EMAIL
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Email",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, // SỬA
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "example@gmail.com"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // INPUT PASSWORD
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Mật khẩu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, // SỬA
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle password",
                                tint = Color.Gray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // QUÊN MẬT KHẨU
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Quên mật khẩu?",
                        style = MaterialTheme.typography.bodyMedium, // Thêm style
                        color = Cinnabar500,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onForgotPassClick)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // NÚT ĐĂNG NHẬP
                PrimaryButton(
                    text = "Đăng nhập",
                    onClick = {
                        // Giả lập hàm signIn, bạn import hàm thực tế của bạn vào
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            // signInWithEmailAndPassword(email, password, auth) ...
                            // Tạm thời gọi success để test UI
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Hoặc",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // GOOGLE SIGN IN
                IconButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier.size(52.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ĐĂNG KÝ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Chưa có tài khoản?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đăng ký",
                        style = MaterialTheme.typography.bodyMedium, // Thêm style
                        color = Cinnabar500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onRegisterClick)
                    )
                }
            }
        }
    }
}