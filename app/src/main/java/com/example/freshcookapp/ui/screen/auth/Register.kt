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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun Register(
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onGithubSignInClick: () -> Unit,
    onPhoneSignInClick: () -> Unit, // Giữ lại để không lỗi signature
    onFacebookSignInClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val authState by authViewModel.authUiState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    ScreenContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 20.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart).offset(x = (-12).dp)) {
                            Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(text = "Tạo tài khoản", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cinnabar500, modifier = Modifier.align(Alignment.Center))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    InputField(label = "Họ và tên", value = fullName, onValueChange = { fullName = it }, placeholder = "Nhập họ tên")
                    InputField(label = "Email", value = email, onValueChange = { email = it }, placeholder = "example@gmail.com")
                    PasswordField(label = "Mật khẩu", value = password, onValueChange = { password = it }, isVisible = passwordVisible, onToggleVisibility = { passwordVisible = !passwordVisible })
                    PasswordField(label = "Nhập lại mật khẩu", value = confirmPassword, onValueChange = { confirmPassword = it }, isVisible = confirmPasswordVisible, onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible })

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Đăng ký",
                        onClick = {
                            if (password != confirmPassword) Toast.makeText(context, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
                            else authViewModel.register(fullName, email, password)
                        },
                        enabled = authState !is AuthUiState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(text = "Hoặc tiếp tục với", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- CỤM NÚT SOCIAL (ĐÃ SỬA) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google
                        IconButton(onClick = onGoogleSignInClick, modifier = Modifier.size(50.dp)) {
                            Image(painter = painterResource(R.drawable.ic_google_logo), contentDescription = "Google", modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.width(20.dp))

                        // Facebook
                        IconButton(onClick = onFacebookSignInClick, modifier = Modifier.size(50.dp)) {
                            Image(
                                painter = painterResource(R.drawable.ic_face),
                                contentDescription = "Facebook",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))

                        // GitHub
                        IconButton(onClick = onGithubSignInClick, modifier = Modifier.size(50.dp)) {
                            Image(
                                painter = painterResource(R.drawable.ic_github),
                                contentDescription = "Github",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Đã có tài khoản?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Đăng nhập", style = MaterialTheme.typography.bodyMedium, color = Cinnabar500, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onLoginClick))
                    }
                }
            }

            if (authState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Cinnabar500)
            }
        }
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        CustomTextField(value = value, onValueChange = onValueChange, placeholder = placeholder)
    }
}

@Composable
fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit, isVisible: Boolean, onToggleVisibility: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        CustomTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "••••••",
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle password", tint = Color.Gray)
                }
            }
        )
    }
}