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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.GrayLight
import com.example.freshcookapp.ui.theme.WorkSans

// 1. Tạo Model cho Thông báo (Chuẩn bị cho dữ liệu thật sau này)
data class NotificationModel(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val message: String,
    val time: String,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 2. Giả lập dữ liệu thật: Danh sách rỗng (Empty List)
    // Sau này bạn sẽ lấy list này từ ViewModel/Firebase
    val notifications = remember { listOf<NotificationModel>() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,

        // --- ÁP DỤNG FIX LỖI GIẬT LAYOUT (MANUAL MODE) ---
        contentWindowInsets = WindowInsets(0.dp),

        topBar = {
            TopAppBar(
                // Đóng băng vị trí TopBar
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        // 3. Logic hiển thị: Nếu rỗng thì hiện màn hình trống, ngược lại hiện list
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

// --- Giao diện khi chưa có thông báo (Empty State) ---
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
        Text(
            text = "Chưa có thông báo nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = "Khi có tương tác mới, chúng sẽ hiện ở đây.",
            fontSize = 14.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// --- Danh sách thông báo (Dùng cho sau này khi có data) ---
@Composable
fun NotificationList(
    notifications: List<NotificationModel>,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Ví dụ cách render (Logic gom nhóm theo ngày làm sau)
        items(notifications) { notification ->
            NotificationItem(notification = notification)
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color.White else Color(0xFFFFF0F0)) // Chưa đọc thì nền hồng nhạt
            .clickable { /* Handle click */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Image(
                painter = rememberAsyncImagePainter(
                    model = notification.userAvatar ?: R.drawable.avatar1 // Ảnh mặc định nếu null
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.userName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = WorkSans,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    fontFamily = WorkSans,
                    color = Color.Gray,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time
            Text(
                text = notification.time,
                fontSize = 12.sp,
                fontFamily = WorkSans,
                color = Color.Gray
            )
        }

        Divider(
            modifier = Modifier.padding(top = 12.dp),
            color = Color(0xFFEEEEEE),
            thickness = 1.dp
        )
    }
}