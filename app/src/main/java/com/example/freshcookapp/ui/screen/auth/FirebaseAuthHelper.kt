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
                    saveUserToFirestore(user) { success ->
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
    auth: FirebaseAuth,
    onResult: (Boolean, String?) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    saveUserToFirestore(user) { success ->
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

private fun saveUserToFirestore(user: FirebaseUser, onResult: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(user.uid)
    val userData = hashMapOf(
        "name" to user.displayName,
        "email" to user.email,
        "photoUrl" to user.photoUrl.toString()
    )

    userRef.set(userData)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}
