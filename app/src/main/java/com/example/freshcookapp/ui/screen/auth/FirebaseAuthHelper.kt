package com.example.freshcookapp.ui.screen.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
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
                    val fullName = user.displayName ?: ""
                    val username = user.email?.split("@")?.firstOrNull() ?: (user.email ?: "")

                    // Lưu vào Firestore
                    saveUserToFirestore(user, fullName, username) { success ->
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
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .setPhotoUri(Uri.parse("https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Favatar_user.png?alt=media&token=1db6c7a8-852f-4271-81df-3f076b38fea6"))
                        .build()
                    user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                        saveUserToFirestore(user, fullName, username) { success ->
                            onResult(success, if (success) user.displayName else "Failed to save user data.")
                        }
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
    fullName: String,
    username: String,
    onResult: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(user.uid)
    val photoUrlToSave = user.photoUrl?.toString() ?: "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Favatar_user.png?alt=media&token=1db6c7a8-852f-4271-81df-3f076b38fea6"

    val userData = hashMapOf(
        "uid" to user.uid,
        "email" to user.email,
        "fullName" to fullName,
        "name" to fullName,
        "username" to username,
        "photoUrl" to (user.photoUrl?.toString() ?: ""),
        "gender" to "Khác",
        "dateOfBirth" to null,
        "followerCount" to 0L,
        "followingCount" to 0L,
        "myDishesCount" to 0L
    )

    userRef.set(userData)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}