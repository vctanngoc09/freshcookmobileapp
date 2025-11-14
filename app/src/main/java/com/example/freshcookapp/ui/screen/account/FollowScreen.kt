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

// Data class to store simplified user information
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
    type: String, // "followers" or "following"
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit = {}
) {
    val firestore = FirebaseFirestore.getInstance()
    var userList by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val title = if (type == "followers") "Followers" else "Following"

    LaunchedEffect(userId, type) {
        isLoading = true
        val collectionPath = "users/$userId/$type"

        firestore.collection(collectionPath).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    userList = emptyList()
                    isLoading = false
                    return@addOnSuccessListener
                }
                val userIds = snapshot.documents.map { it.id }
                firestore.collection("users").whereIn("uid", userIds).get()
                    .addOnSuccessListener { userDocs ->
                        userList = userDocs.mapNotNull { doc ->
                            doc.toObject<UserInfo>().copy(uid = doc.id)
                        }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        isLoading = false
                    }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
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
                    containerColor = Color.White
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
                Text("Không có ai trong danh sách này", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(userList) { user ->
                        UserItem(user = user, onProfileClick = onProfileClick)
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserInfo, onProfileClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick(user.uid) }
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
                fontFamily = WorkSans
            )
            if (user.username.isNotBlank()) {
                Text(
                    text = "@${user.username}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontFamily = WorkSans
                )
            }
        }
    }
}
