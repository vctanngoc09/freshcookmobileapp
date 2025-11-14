package com.example.freshcookapp.ui.screen.account

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(
    userId: String, // ID của người dùng mà chúng ta đang xem
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var fullName by remember { mutableStateOf("...") }
    var username by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var followerCount by remember { mutableStateOf(0L) }
    var followingCount by remember { mutableStateOf(0L) }
    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Lắng nghe dữ liệu người dùng được xem trong thời gian thực
    LaunchedEffect(userId) {
        firestore.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                fullName = snapshot.getString("fullName") ?: ""
                username = snapshot.getString("username") ?: ""
                photoUrl = snapshot.getString("photoUrl")
                followerCount = snapshot.getLong("followerCount") ?: 0L
                followingCount = snapshot.getLong("followingCount") ?: 0L
                isLoading = false
            } else {
                isLoading = false
            }
        }
    }

    // Lắng nghe trạng thái follow trong thời gian thực
    DisposableEffect(currentUserId, userId) {
        val listener = if (currentUserId != null) {
            firestore.collection("users").document(currentUserId)
                .collection("following").document(userId)
                .addSnapshotListener { document, _ ->
                    isFollowing = document != null && document.exists()
                }
        } else {
            null
        }
        onDispose {
            listener?.remove()
        }
    }

    val onFollowClick: () -> Unit = {
        if (currentUserId == null) {
            Toast.makeText(context, "Bạn cần đăng nhập để thực hiện", Toast.LENGTH_SHORT).show()
        } else if (currentUserId == userId) {
            Toast.makeText(context, "Bạn không thể tự theo dõi chính mình", Toast.LENGTH_SHORT).show()
        } else {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val viewedUserRef = firestore.collection("users").document(userId)
            val followingSubCollection = currentUserRef.collection("following").document(userId)
            val followerSubCollection = viewedUserRef.collection("followers").document(currentUserId)

            firestore.runTransaction { transaction ->
                val isCurrentlyFollowing = transaction.get(followingSubCollection).exists()

                if (isCurrentlyFollowing) {
                    // --- Bỏ theo dõi ---
                    transaction.delete(followingSubCollection)
                    transaction.delete(followerSubCollection)
                    transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
                    transaction.update(viewedUserRef, "followerCount", FieldValue.increment(-1))
                } else {
                    // --- Theo dõi ---
                    transaction.set(followingSubCollection, mapOf("timestamp" to FieldValue.serverTimestamp()))
                    transaction.set(followerSubCollection, mapOf("timestamp" to FieldValue.serverTimestamp()))
                    transaction.update(currentUserRef, "followingCount", FieldValue.increment(1))
                    transaction.update(viewedUserRef, "followerCount", FieldValue.increment(1))
                }
                !isCurrentlyFollowing // Trả về trạng thái mới để dùng trong onSuccessListener
            }.addOnSuccessListener { nowFollowing ->
                val message = if (nowFollowing) "Đã theo dõi $username" else "Đã bỏ theo dõi $username"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(username) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Header
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Image(
                        painter = rememberAsyncImagePainter(model = photoUrl ?: R.drawable.avatar1),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    if (username.isNotBlank()) {
                        Text("@$username", fontSize = 16.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(count = followerCount.toString(), label = "Followers")
                        StatItem(count = followingCount.toString(), label = "Following")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Follow Button
                if (currentUserId != userId) { // Ẩn nút nếu đang xem hồ sơ của chính mình
                    item {
                        Button(
                            onClick = onFollowClick,
                            modifier = Modifier.fillMaxWidth(0.6f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color.LightGray else Cinnabar500,
                                contentColor = if (isFollowing) Color.Black else Color.White
                            )
                        ) {
                            Text(if (isFollowing) "Đang theo dõi" else "Theo dõi")
                        }
                    }
                }
            }
        }
    }
}

// StatItem được tùy chỉnh cho màn hình này
@Composable
private fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
