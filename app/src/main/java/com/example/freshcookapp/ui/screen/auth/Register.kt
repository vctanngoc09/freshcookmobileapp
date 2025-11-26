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
fun Register(
    onRegisterSuccess: () -> Unit,
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
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    ScreenContainer {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // FIX LỖI: Đẩy nội dung xuống khỏi tai thỏ
                .imePadding(),      // Đẩy nội dung lên khi bàn phím hiện
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                // HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
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

                    Text(
                        text = "Tạo tài khoản",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Cinnabar500,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // FORM INPUTS
                InputField(label = "Họ và tên", value = fullName, onValueChange = { fullName = it }, placeholder = "Nhập họ và tên")
                InputField(label = "Số điện thoại", value = phone, onValueChange = { phone = it }, placeholder = "Nhập số điện thoại")
                InputField(label = "Email", value = email, onValueChange = { email = it }, placeholder = "example@gmail.com")

                // MẬT KHẨU
                PasswordField(
                    label = "Mật khẩu",
                    value = password,
                    onValueChange = { password = it },
                    isVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                // XÁC NHẬN MẬT KHẨU
                PasswordField(
                    label = "Nhập lại mật khẩu",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    isVisible = confirmPasswordVisible,
                    onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // NÚT ĐĂNG KÝ
                PrimaryButton(
                    text = "Tạo tài khoản",
                    onClick = {
                        if (password == confirmPassword) {
                            val username = email.split("@").firstOrNull() ?: email
                            createUserWithEmailAndPassword(
                                email = email,
                                password = password,
                                fullName = fullName,
                                username = username,
                                auth = auth
                            ) { success, message ->
                                if (success) {
                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    onRegisterSuccess()
                                } else {
                                    Toast.makeText(context, "Đăng ký thất bại: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Hoặc", color = androidx.compose.ui.graphics.Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                IconButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier.size(50.dp)
                ) {
                    Image(painterResource(R.drawable.ic_google_logo), contentDescription = "Google", modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Đã có tài khoản?", color = androidx.compose.ui.graphics.Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đăng nhập",
                        color = Cinnabar500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onLoginClick)
                    )
                }
            }
        }
    }
}

// Component phụ để code gọn hơn
@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Black)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(value = value, onValueChange = onValueChange, placeholder = placeholder)
    }
}

@Composable
fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit, isVisible: Boolean, onToggleVisibility: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Black)
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "••••••",
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle password"
                    )
                }
            }
        )
    }
}