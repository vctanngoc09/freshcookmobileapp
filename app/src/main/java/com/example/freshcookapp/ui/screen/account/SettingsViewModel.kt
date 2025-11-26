package com.example.freshcookapp.ui.screen.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Xóa dữ liệu cá nhân trong Room Database (Món yêu thích, Lịch sử)
                // Hàm clearLocalUserData này đã được thêm vào Repository ở các bước trước
                repository.clearLocalUserData()
                Log.d("SettingsViewModel", "Đã dọn dẹp dữ liệu local")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Lỗi khi dọn dẹp DB: ${e.message}")
            } finally {
                // 2. Đăng xuất khỏi Firebase Auth (Luôn chạy dù xóa DB lỗi hay không)
                auth.signOut()

                // 3. Báo cho UI biết đã xong để chuyển màn hình
                onLogoutSuccess()
            }
        }
    }
}