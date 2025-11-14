package com.example.freshcookapp.ui.screen.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    // Lấy thông tin từ user Google
                    val fullName = user.displayName ?: ""
                    // Tạo username từ email
                    val username = user.email?.split("@")?.firstOrNull() ?: (user.email ?: "")

                    // Sửa lệnh gọi saveUserToFirestore để truyền thêm thông tin
                    saveUserToFirestore(user, fullName, username) { success -> // <-- Sửa ở đây
                        onResult(success, if (success) user.displayName else "Failed to save user data.")
                    }
                } else {
                    onResult(false, "Authentication successful, but user is null.")
                }
            } else {
                onResult(false, task.exception?.message)
            }
        }
}

fun sendPasswordResetEmail(
    email: String,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, null)
            } else {
                onResult(false, task.exception?.message)
            }
        }
}

fun createUserWithEmailAndPassword(
    email: String,
    password: String,
    fullName: String, // <-- Thêm fullName
    username: String, // <-- Thêm username
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    // Truyền 2 giá trị mới vào hàm save
                    saveUserToFirestore(user, fullName, username) { success -> // <-- Sửa ở đây
                        onResult(success, if (success) user.displayName else "Failed to save user data.")
                    }
                } else {
                    onResult(false, "Account creation successful, but user is null.")
                }
            } else {
                onResult(false, task.exception?.message)
            }
        }
}

fun signInWithEmailAndPassword(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, auth.currentUser?.displayName)
            } else {
                onResult(false, task.exception?.message)
            }
        }
}

private fun saveUserToFirestore(
    user: FirebaseUser,
    fullName: String, // <-- Thêm tham số
    username: String, // <-- Thêm tham số
    onResult: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(user.uid)

    // Tạo đối tượng người dùng với tất cả các trường
    val userData = hashMapOf(
        "uid" to user.uid,
        "email" to user.email,
        "fullName" to fullName, // <-- Sử dụng giá trị từ tham số
        "name" to fullName,     // <-- Sử dụng giá trị từ tham số (giống trong ảnh của bạn)
        "username" to username, // <-- Sử dụng giá trị từ tham số
        "photoUrl" to (user.photoUrl?.toString() ?: ""), // Xử lý trường hợp photoUrl bị null
        "gender" to "Khác",      // Giá trị mặc định
        "dateOfBirth" to null,     // Giá trị mặc định

        // --- CÁC TRƯỜNG MỚI BẠN MUỐN THÊM ---
        "followerCount" to 0L,  // Dùng 0L (kiểu Long) cho an toàn
        "followingCount" to 0L,
        "myDishesCount" to 0L
        // ------------------------------------
    )

    // Dùng set(userData) để tạo hoặc ghi đè document
    userRef.set(userData)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}