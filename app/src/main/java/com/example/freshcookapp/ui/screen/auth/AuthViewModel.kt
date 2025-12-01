package com.example.freshcookapp.ui.screen.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState = _authUiState.asStateFlow()

    // --- BIẾN CHO PHONE AUTH ---
    var verificationId: String = ""
    var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    // --- LOGIN EMAIL ---
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Vui lòng nhập đầy đủ email và mật khẩu.")
            return
        }
        _authUiState.value = AuthUiState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authUiState.value = AuthUiState.Success
                } else {
                    val errorMessage = task.exception?.message ?: "Đăng nhập thất bại."
                    _authUiState.value = AuthUiState.Error(errorMessage)
                }
            }
    }

    // --- REGISTER EMAIL ---
    fun register(fullName: String, email: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Vui lòng nhập đầy đủ thông tin.")
            return
        }
        _authUiState.value = AuthUiState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        // Gọi hàm save từ Helper (hoặc copy logic vào đây)
                        saveUserToFirestore(firebaseUser, fullName, email.substringBefore('@')) { success ->
                            if (success) _authUiState.value = AuthUiState.Success
                            else _authUiState.value = AuthUiState.Error("Lỗi lưu thông tin.")
                        }
                    } else {
                        _authUiState.value = AuthUiState.Error("Không lấy được thông tin người dùng.")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Đăng ký thất bại."
                    _authUiState.value = AuthUiState.Error(errorMessage)
                }
            }
    }

    // --- PHONE AUTH: GỬI OTP ---
    fun sendOtp(activity: Activity, phoneNumber: String, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        _authUiState.value = AuthUiState.Loading
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // --- PHONE AUTH: XÁC THỰC OTP ---
    fun verifyOtp(code: String) {
        if (verificationId.isEmpty()) {
            _authUiState.value = AuthUiState.Error("Lỗi: Mất mã xác thực, vui lòng gửi lại.")
            return
        }
        _authUiState.value = AuthUiState.Loading
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    // Xử lý lưu user sau khi login thành công
                    if (user != null) {
                        val fullName = user.phoneNumber ?: "User Phone"
                        val username = user.uid
                        saveUserToFirestore(user, fullName, username) { success ->
                            if(success) _authUiState.value = AuthUiState.Success
                            else _authUiState.value = AuthUiState.Error("Lỗi lưu dữ liệu.")
                        }
                    }
                } else {
                    _authUiState.value = AuthUiState.Error(task.exception?.message ?: "Mã OTP không đúng.")
                }
            }
    }

    fun resetState() {
        _authUiState.value = AuthUiState.Idle
    }
}