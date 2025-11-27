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
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
// Xóa import Black
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
    var phone by remember { mutableStateOf("") } // Nếu không quá cần thiết, bạn có thể bỏ trường này để giao diện thoáng hơn
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
                .statusBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Padding ngang 24dp để nội dung không sát lề
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 20.dp)
        ) {
            item {
                // HEADER GỌN GÀNG HƠN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp) // Giảm padding header
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (-12).dp) // Căn icon sát lề trái
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
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

                Spacer(modifier = Modifier.height(12.dp)) // Giảm khoảng cách

                // FORM INPUTS (Khoảng cách giữa các ô đã được giảm trong InputField bên dưới)
                InputField(label = "Họ và tên", value = fullName, onValueChange = { fullName = it }, placeholder = "Nhập họ tên")
                InputField(label = "Số điện thoại", value = phone, onValueChange = { phone = it }, placeholder = "Nhập SĐT")
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

                Spacer(modifier = Modifier.height(24.dp)) // Giảm từ 32 xuống 24

                // NÚT ĐĂNG KÝ
                PrimaryButton(
                    text = "Đăng ký",
                    onClick = {
                        // Logic đăng ký giữ nguyên
                        if (password == confirmPassword) {
                            val username = email.split("@").firstOrNull() ?: email
                            // Gọi hàm createUser... (đảm bảo bạn đã import hàm này)
                            /* createUserWithEmailAndPassword(
                                email = email, password = password, fullName = fullName,
                                username = username, auth = auth
                            ) { success, message -> ... }
                            */
                            // Code giả lập để test UI
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                onRegisterSuccess()
                            }
                        } else {
                            Toast.makeText(context, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp)) // Giảm khoảng cách

                // PHẦN HOẶC / GOOGLE / ĐĂNG NHẬP (Gom gọn lại)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Hoặc", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                IconButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier.size(44.dp) // Nút nhỏ lại xíu
                ) {
                    Image(painterResource(R.drawable.ic_google_logo), contentDescription = "Google", modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Đã có tài khoản?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cinnabar500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onLoginClick)
                    )
                }
            }
        }
    }
}

// Component phụ đã được tinh chỉnh khoảng cách cho gọn (Compact)
@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    // Giảm padding bottom từ 12.dp xuống 8.dp
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge, // Dùng font nhỏ hơn xíu cho Label
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface // Tự động đổi màu
        )
        Spacer(modifier = Modifier.height(4.dp)) // Giảm khoảng cách Label với Ô nhập
        CustomTextField(value = value, onValueChange = onValueChange, placeholder = placeholder)
    }
}

@Composable
fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit, isVisible: Boolean, onToggleVisibility: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        CustomTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "••••••",
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle password",
                        tint = Color.Gray
                    )
                }
            }
        )
    }
}