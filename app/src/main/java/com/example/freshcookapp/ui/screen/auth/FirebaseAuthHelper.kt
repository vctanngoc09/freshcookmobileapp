// Vị trí: com.example.freshcookapp.auth/FirebaseAuthHelper.kt
package com.example.freshcookapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

// Hàm này là tiêu chuẩn, dùng để đổi IdToken của Google lấy thông tin Firebase
fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit // (thành công?, tên_người_dùng_hoặc_lỗi)
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Đăng nhập thành công, trả về tên
                onResult(true, auth.currentUser?.displayName)
            } else {
                // Đăng nhập thất bại, trả về lỗi
                onResult(false, task.exception?.message)
            }
        }
}