package com.example.freshcookapp.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun Login(
    authViewModel: AuthViewModel = viewModel(), // Nhận ViewModel
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

    // SỬA Ở ĐÂY: Dùng đúng tên biến và kiểu dữ liệu đã được đổi
    val authState by authViewModel.authUiState.collectAsState()

    // Xử lý các thay đổi trạng thái
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
                authViewModel.resetState() // Reset lại trạng thái để không bị lặp lại
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> { /* Không cần làm gì với Idle hoặc Loading ở đây */ }
        }
    }

    ScreenContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                            modifier = Modifier.offset(x = (-12).dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = "Back",
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
                        color = Cinnabar500,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Hãy cùng quay trở lại nhé!",
                        style = MaterialTheme.typography.bodyLarge,
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                        style = MaterialTheme. typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
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
                            style = MaterialTheme.typography.bodyMedium,
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
                            // Gọi hàm login từ ViewModel
                            authViewModel.login(email, password)
                        },
                        // Vô hiệu hóa nút khi đang loading
                        enabled = authState !is AuthUiState.Loading // SỬA Ở ĐÂY
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = Cinnabar500,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onRegisterClick)
                        )
                    }
                }
            }

            // Hiển thị vòng quay loading ở giữa màn hình
            if (authState is AuthUiState.Loading) { // SỬA Ở ĐÂY
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Cinnabar500
                )
            }
        }
    }
}
