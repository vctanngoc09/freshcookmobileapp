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
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPassClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onGithubSignInClick: () -> Unit,
    onPhoneSignInClick: () -> Unit, // Giữ lại để không lỗi signature
    onFacebookSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val authState by authViewModel.authUiState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
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
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Header Back Button
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-12).dp)) {
                            Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Rất vui được gặp lại bạn!", style = MaterialTheme.typography.headlineMedium, color = Cinnabar500, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Left)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Hãy cùng quay trở lại nhé!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Left)

                    Spacer(modifier = Modifier.height(40.dp))

                    Text("Email", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(value = email, onValueChange = { email = it }, placeholder = "example@gmail.com")

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Mật khẩu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "••••••",
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle password", tint = Color.Gray)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(text = "Quên mật khẩu?", style = MaterialTheme.typography.bodyMedium, color = Cinnabar500, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onForgotPassClick))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(text = "Đăng nhập", onClick = { authViewModel.login(email, password) }, enabled = authState !is AuthUiState.Loading)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Hoặc đăng nhập với", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                    Spacer(modifier = Modifier.height(20.dp))

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

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Chưa có tài khoản?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Đăng ký", style = MaterialTheme.typography.bodyMedium, color = Cinnabar500, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onRegisterClick))
                    }
                }
            }

            if (authState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Cinnabar500)
            }
        }
    }
}