package com.example.freshcookapp.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Import đầy đủ layout
import androidx.compose.foundation.lazy.LazyColumn // Dùng LazyColumn để cuộn được trên màn hình nhỏ
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
import com.example.freshcookapp.ui.theme.Black
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
        // Dùng LazyColumn để hỗ trợ cuộn khi bàn phím hiện lên
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // FIX LỖI: Tránh bị tai thỏ che
                .imePadding(), // Tránh bị bàn phím che
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 24.dp) // Thêm padding đáy
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // HEADER: Nút Back
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

                Spacer(modifier = Modifier.height(24.dp))

                // TEXT CHÀO MỪNG
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Rất vui được gặp lại bạn!",
                    style = MaterialTheme.typography.headlineMedium.copy(color = Cinnabar500), // Dùng headline to hơn chút
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Hãy cùng quay trở lại nhé!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Black,
                    textAlign = TextAlign.Left
                )

                Spacer(modifier = Modifier.height(40.dp))

                // INPUT EMAIL
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Email",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Black,
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
                    color = Black,
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
                                contentDescription = "Toggle password"
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
                        color = Cinnabar500,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onForgotPassClick)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // NÚT ĐĂNG NHẬP
                PrimaryButton(
                    text = "Đăng nhập",
                    onClick = {
                        signInWithEmailAndPassword(email, password, auth) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "Đăng nhập thất bại: ${message ?: "Lỗi không xác định"}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Hoặc", color = androidx.compose.ui.graphics.Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                // GOOGLE SIGN IN
                IconButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier.size(50.dp) // Tăng kích thước nút Google cho dễ bấm
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
                    Text(text = "Chưa có tài khoản?", color = androidx.compose.ui.graphics.Color.Gray)
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
}