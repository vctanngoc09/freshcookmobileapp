package com.example.freshcookapp.ui.screen.auth

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// --- GOOGLE AUTH ---
fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                handleAuthSuccess(auth.currentUser, onResult)
            } else {
                onResult(false, task.exception?.message)
            }
        }
}

// --- GITHUB AUTH (MỚI) ---
fun firebaseAuthWithGitHub(
    activity: Activity,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    val provider = OAuthProvider.newBuilder("github.com")
    // provider.addCustomParameter("login", "your-email@example.com")

    val pendingResultTask = auth.pendingAuthResult
    if (pendingResultTask != null) {
        pendingResultTask
            .addOnSuccessListener { authResult ->
                handleAuthSuccess(authResult.user, onResult)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    } else {
        auth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener { authResult ->
                handleAuthSuccess(authResult.user, onResult)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
}

// --- CÁC HÀM CŨ (EMAIL/PASSWORD) ---

fun sendPasswordResetEmail(
    email: String,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) onResult(true, null)
            else onResult(false, task.exception?.message)
        }
}

fun createUserWithEmailAndPassword(
    email: String,
    password: String,
    fullName: String,
    username: String,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    val defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Favatar_user.png?alt=media&token=1db6c7a8-852f-4271-81df-3f076b38fea6"
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .setPhotoUri(Uri.parse(defaultAvatar))
                        .build()
                    user.updateProfile(profileUpdates).addOnCompleteListener {
                        saveUserToFirestore(user, fullName, username) { success ->
                            onResult(success, if (success) user.displayName else "Failed to save user data.")
                        }
                    }
                } else {
                    onResult(false, "User is null.")
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
            if (task.isSuccessful) onResult(true, auth.currentUser?.displayName)
            else onResult(false, task.exception?.message)
        }
}

// --- HELPER FUNCTIONS ---

// Xử lý chung sau khi đăng nhập thành công (Google, GitHub, Phone)
fun handleAuthSuccess(user: FirebaseUser?, onResult: (Boolean, String?) -> Unit) {
    if (user != null) {
        // Lấy tên hiển thị, nếu null thì lấy phần đầu email hoặc số điện thoại
        val fullName = user.displayName ?: user.email?.substringBefore("@") ?: user.phoneNumber ?: "User"
        val username = user.email?.split("@")?.firstOrNull() ?: user.uid

        saveUserToFirestore(user, fullName, username) { success ->
            onResult(success, if (success) fullName else "Lỗi lưu dữ liệu.")
        }
    } else {
        onResult(false, "User is null.")
    }
}

// Lưu hoặc Cập nhật user vào Firestore (Dùng SetOptions.merge để không mất dữ liệu cũ)
fun saveUserToFirestore(
    user: FirebaseUser,
    fullName: String,
    username: String,
    onResult: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(user.uid)
    val defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Favatar_user.png?alt=media&token=1db6c7a8-852f-4271-81df-3f076b38fea6"

    val photoUrlToSave = user.photoUrl?.toString() ?: defaultAvatar

    val userData = hashMapOf<String, Any?>(
        "uid" to user.uid,
        "email" to (user.email ?: ""),
        "phoneNumber" to (user.phoneNumber ?: ""),
        "fullName" to fullName,
        "name" to fullName,
        "username" to username,
        "photoUrl" to photoUrlToSave
    )

    // Kiểm tra xem document đã tồn tại chưa
    userRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            // Nếu là user mới -> Thêm các trường khởi tạo
            userData["gender"] = "Khác"
            userData["dateOfBirth"] = null
            userData["followerCount"] = 0L
            userData["followingCount"] = 0L
            userData["myDishesCount"] = 0L
        }

        // Merge: Chỉ cập nhật các trường có trong userData, giữ nguyên các trường khác (như followerCount cũ)
        userRef.set(userData, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}