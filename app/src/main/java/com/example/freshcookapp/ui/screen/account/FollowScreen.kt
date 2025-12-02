package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

data class UserInfo(
    val uid: String = "",
    val fullName: String = "",
    val username: String = "",
    val photoUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    userId: String,
    type: String, // "followers" hoặc "following"
    onBackClick: () -> Unit,
    onUserClick: (userId: String) -> Unit // <-- THÊM THAM SỐ NÀY
) {
    val firestore = FirebaseFirestore.getInstance()
    var userList by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val title = if (type == "followers") "Followers" else "Following"

    // Logic lấy dữ liệu giữ nguyên
    DisposableEffect(userId, type) {
        val collectionPath = "users/$userId/$type"
        val listener = firestore.collection(collectionPath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    userList = emptyList()
                    isLoading = false
                    return@addSnapshotListener
                }
                val userIds = snapshot.documents.map { it.id }
                if (userIds.isNotEmpty()) {
                    firestore.collection("users").whereIn("uid", userIds)
                        .get()
                        .addOnSuccessListener { userDocs ->
                            userList = userDocs.mapNotNull { it.toObject<UserInfo>() }
                            isLoading = false
                        }
                        .addOnFailureListener {
                            isLoading = false
                        }
                } else {
                    userList = emptyList()
                    isLoading = false
                }
            }
        onDispose { listener.remove() }
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        title,
                        color = Cinnabar500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Cinnabar500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (userList.isEmpty()) {
                Text(
                    text = "Chưa có ai trong danh sách này",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(userList) { user ->
                        // SỬA Ở ĐÂY: Truyền callback onUserClick vào UserItem
                        UserItem(user = user, onUserClick = onUserClick)
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserInfo, onUserClick: (String) -> Unit) { // <-- SỬA TÊN THAM SỐ
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // SỬA Ở ĐÂY: Gọi onUserClick với uid của user
            .clickable { onUserClick(user.uid) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = user.photoUrl ?: R.drawable.avatar1),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = user.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = WorkSans,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (user.username.isNotBlank()) {
                Text(
                    text = "@${user.username}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = WorkSans
                )
            }
        }
    }
}
