package com.example.freshcookapp.ui.screen.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- SỬA LẠI TÊN CHO RÕ NGHĨA HƠN ---
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

    // --- HÀM MỚI ĐỂ ĐĂNG KÝ ---
    fun register(fullName: String, email: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Vui lòng nhập đầy đủ thông tin.")
            return
        }
        _authUiState.value = AuthUiState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Đăng ký thành công, bây giờ lưu thông tin vào Firestore
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val user = hashMapOf(
                            "uid" to firebaseUser.uid,
                            "fullName" to fullName,
                            "email" to email,
                            "username" to email.substringBefore('@'), // Tên người dùng mặc định
                            "photoUrl" to null // URL ảnh mặc định
                        )

                        firestore.collection("users").document(firebaseUser.uid)
                            .set(user)
                            .addOnSuccessListener {
                                _authUiState.value = AuthUiState.Success
                            }
                            .addOnFailureListener { e ->
                                val errorMessage = e.message ?: "Lỗi khi lưu thông tin người dùng."
                                _authUiState.value = AuthUiState.Error(errorMessage)
                            }
                    } else {
                         _authUiState.value = AuthUiState.Error("Không lấy được thông tin người dùng sau khi tạo.")
                    }
                } else {
                    // Đăng ký thất bại
                    val errorMessage = task.exception?.message ?: "Đăng ký thất bại."
                    _authUiState.value = AuthUiState.Error(errorMessage)
                }
            }
    }

    fun resetState() {
        _authUiState.value = AuthUiState.Idle
    }
}
