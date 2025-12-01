package com.example.freshcookapp.ui.screen.auth

import android.app.Activity
import android.net.Uri
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
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

// --- FACEBOOK AUTH (MỚI) ---
fun firebaseAuthWithFacebook(
    token: AccessToken,
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    val credential = FacebookAuthProvider.getCredential(token.token)
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

// Xử lý chung sau khi đăng nhập thành công (Google, GitHub, Phone, Facebook)
fun handleAuthSuccess(user: FirebaseUser?, onResult: (Boolean, String?) -> Unit) {
    if (user != null) {
        // Lấy tên hiển thị
        val fullName = user.displayName ?: user.email?.substringBefore("@") ?: user.phoneNumber ?: "User"

        // 🔥 CẢI THIỆN: Tạo username đẹp hơn cho Facebook Login
        val username = when {
            // Nếu có email -> dùng phần trước @
            user.email != null -> user.email!!.split("@").firstOrNull() ?: user.uid
            // Nếu có displayName (Facebook, Google) -> chuyển thành username
            user.displayName != null -> {
                user.displayName!!
                    .lowercase()
                    .replace(" ", "")
                    .replace(Regex("[^a-z0-9]"), "") // Chỉ giữ chữ và số
                    .take(20) // Giới hạn 20 ký tự
                    .ifEmpty { user.uid.take(8) } // Nếu rỗng thì dùng 8 ký tự đầu của UID
            }
            // Nếu có phone number
            user.phoneNumber != null -> user.phoneNumber!!.replace("+", "").take(10)
            // Fallback: dùng 8 ký tự đầu của UID
            else -> user.uid.take(8)
        }

        // ✅ QUAN TRỌNG: Trả về success NGAY LẬP TỨC (như Instagram, Facebook app)
        // Vì Firebase Authentication đã thành công → User có thể dùng app
        onResult(true, fullName)

        // Lưu vào Firestore ở background (không ảnh hưởng đến login flow)
        try {
            saveUserToFirestore(user, fullName, username) { success ->
                if (!success) {
                    android.util.Log.w("FirebaseAuth", "⚠️ Firestore save failed, will retry later")
                } else {
                    android.util.Log.d("FirebaseAuth", "✅ User saved to Firestore successfully")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAuth", "❌ Error saving to Firestore: ${e.message}")
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
    userRef.get()
        .addOnSuccessListener { document ->
            if (!document.exists()) {
                // Nếu là user mới -> Thêm các trường khởi tạo
                userData["gender"] = "Khác"
                userData["dateOfBirth"] = null
                userData["followerCount"] = 0L
                userData["followingCount"] = 0L
                userData["myDishesCount"] = 0L
            }

            // Merge: Chỉ cập nhật các trường có trong userData, giữ nguyên các trường khác
            userRef.set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    android.util.Log.d("FirebaseAuth", "✅ User saved to Firestore successfully")
                    onResult(true)
                }
                .addOnFailureListener { e ->
                    android.util.Log.w("FirebaseAuth", "⚠️ Firestore save failed: ${e.message}")
                    onResult(false)
                }
        }
        .addOnFailureListener { e ->
            // ⚠️ Nếu get() fail (do network issue), vẫn cố gắng save
            android.util.Log.w("FirebaseAuth", "⚠️ Firestore get failed, trying to save anyway: ${e.message}")

            // Thêm các trường mặc định (giả sử là user mới)
            userData["gender"] = "Khác"
            userData["dateOfBirth"] = null
            userData["followerCount"] = 0L
            userData["followingCount"] = 0L
            userData["myDishesCount"] = 0L

            userRef.set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    android.util.Log.d("FirebaseAuth", "✅ User saved to Firestore successfully (fallback)")
                    onResult(true)
                }
                .addOnFailureListener { e2 ->
                    android.util.Log.e("FirebaseAuth", "❌ Firestore save failed completely: ${e2.message}")
                    onResult(false)
                }
        }
}