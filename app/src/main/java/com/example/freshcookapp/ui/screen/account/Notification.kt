package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans

// Cập nhật Model: Thêm recipeId để biết bấm vào đi đâu
data class NotificationModel(
    val id: String,
    val userId: String, // ID người gửi
    val userName: String,
    val userAvatar: String?,
    val message: String,
    val time: String,
    val isRead: Boolean = false,
    val recipeId: String? = null // Null nếu là thông báo Follow
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // KẾT NỐI VIEWMODEL
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = "Thông báo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WorkSans,
                        color = Cinnabar500
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Cinnabar500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            EmptyNotificationState(modifier = Modifier.padding(innerPadding))
        } else {
            NotificationList(
                notifications = notifications,
                paddingValues = innerPadding
            )
        }
    }
}

// Giữ nguyên EmptyNotificationState ...
@Composable
fun EmptyNotificationState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsOff,
            contentDescription = "No Notifications",
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chưa có thông báo nào", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("Khi có tương tác mới, chúng sẽ hiện ở đây.", fontSize = 14.sp, color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
fun NotificationList(
    notifications: List<NotificationModel>,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        items(notifications) { notification ->
            NotificationItem(notification = notification)
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color.White else Color(0xFFFFF9F9)) // Màu nền nhạt nếu chưa đọc
            .clickable {
                // Xử lý click sau này (Ví dụ: Navigate tới RecipeDetail)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = notification.userAvatar ?: R.drawable.avatar1),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Tên người gửi và nội dung
                Text(
                    text = notification.userName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = WorkSans,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    fontFamily = WorkSans,
                    color = Color.DarkGray,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Thời gian
                Text(
                    text = notification.time,
                    fontSize = 11.sp,
                    fontFamily = WorkSans,
                    color = Color.Gray
                )
            }
        }
        Divider(modifier = Modifier.padding(top = 12.dp), color = Color(0xFFEEEEEE), thickness = 1.dp)
    }
}