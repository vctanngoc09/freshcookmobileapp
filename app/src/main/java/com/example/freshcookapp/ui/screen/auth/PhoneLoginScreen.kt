package com.example.freshcookapp.ui.screen.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthProvider

@Composable
fun PhoneLoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by authViewModel.authUiState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }

    // Callback xử lý kết quả gửi SMS từ Firebase
    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                // Tự động xác thực nếu cần
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(context, "Lỗi gửi mã: ${e.message}", Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                authViewModel.verificationId = verificationId
                authViewModel.forceResendingToken = token
                isCodeSent = true
                authViewModel.resetState() // Tắt trạng thái loading
                Toast.makeText(context, "Đã gửi mã OTP!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lắng nghe trạng thái thành công/thất bại
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
            authViewModel.resetState()
        } else if (uiState is AuthUiState.Error) {
            Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
            authViewModel.resetState()
        }
    }

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Đăng nhập SĐT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Cinnabar500
            )

            Spacer(Modifier.height(30.dp))

            if (!isCodeSent) {
                // --- BƯỚC 1: NHẬP SỐ ĐIỆN THOẠI ---
                Text("Nhập số điện thoại của bạn", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = "0912345678"
                )

                Spacer(Modifier.height(20.dp))

                PrimaryButton(
                    text = "Gửi mã xác thực",
                    enabled = uiState !is AuthUiState.Loading,
                    onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            // 🔥 FIX FORMAT SỐ ĐIỆN THOẠI CHUẨN 🔥
                            val formattedPhone = when {
                                phoneNumber.startsWith("+84") -> phoneNumber // Đã chuẩn
                                phoneNumber.startsWith("0") -> "+84${phoneNumber.substring(1)}" // Bỏ số 0 đầu
                                else -> "+84$phoneNumber" // Trường hợp nhập thiếu số 0 (vd: 355...)
                            }

                            // Log ra để kiểm tra
                            android.util.Log.d("PhoneAuth", "Sending OTP to: $formattedPhone")

                            authViewModel.sendOtp(context as Activity, formattedPhone, callbacks)
                        } else {
                            Toast.makeText(context, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                // --- BƯỚC 2: NHẬP OTP ---
                Text("Nhập mã OTP gửi tới $phoneNumber", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    placeholder = "123456"
                )

                Spacer(Modifier.height(20.dp))

                // 🔥 Đã sửa: Gọi tên tham số onClick rõ ràng
                PrimaryButton(
                    text = "Xác thực",
                    enabled = uiState !is AuthUiState.Loading,
                    onClick = {
                        if (otpCode.length == 6) {
                            authViewModel.verifyOtp(otpCode)
                        } else {
                            Toast.makeText(context, "Mã OTP phải có 6 số", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                TextButton(onClick = { isCodeSent = false }) {
                    Text("Nhập lại số điện thoại", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = Cinnabar500)
            } else {
                TextButton(onClick = onBackClick) {
                    Text("Quay lại", color = Cinnabar500)
                }
            }
        }
    }
}