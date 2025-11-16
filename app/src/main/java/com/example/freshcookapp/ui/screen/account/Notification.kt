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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.GrayLight
import com.example.freshcookapp.ui.theme.WorkSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 1. Dùng Scaffold thay vì Column
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            // 2. Dùng TopAppBar chuẩn
            TopAppBar(
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
    ) { innerPadding -> // 3. Lấy innerPadding
        // 4. Áp dụng innerPadding cho nội dung
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hôm nay section
            item {
                NotificationSectionHeader(
                    title = "Hôm nay",
                    onClearClick = {}
                )
            }

            // Sample notifications for today
            items(2) { index ->
                NotificationItem(
                    userName = "Maia Adam",
                    message = "thích \"Cà chiên\" của bạn",
                    time = "5p trước",
                    profileImage = null
                )
            }

            // Hôm qua section
            item {
                NotificationSectionHeader(
                    title = "Hôm qua",
                    onClearClick = {}
                )
            }

            // Sample notifications for yesterday
            items(5) { index ->
                NotificationItem(
                    userName = "Maia Adam",
                    message = "liked your \"Honey pancakes\"",
                    time = if (index == 0) "1p trước" else "1 day ago",
                    profileImage = null
                )
            }
        }
    }
}

// ... (Các Composable NotificationSectionHeader và NotificationItem giữ nguyên) ...

@Composable
fun NotificationSectionHeader(
    title: String,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_popup_reminder),
                contentDescription = null,
                tint = Cinnabar500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = WorkSans,
                color = Color.Black
            )
        }
        Text(
            text = "Xóa tất cả",
            fontSize = 14.sp,
            fontFamily = WorkSans,
            color = Cinnabar500,
            modifier = Modifier.clickable { onClearClick() }
        )
    }
}

@Composable
fun NotificationItem(
    userName: String,
    message: String,
    time: String,
    profileImage: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                // Placeholder for profile image
                if (profileImage != null) {
                    Image(
                        painter = painterResource(id = profileImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = WorkSans,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontFamily = WorkSans,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time
            Text(
                text = time,
                fontSize = 12.sp,
                fontFamily = WorkSans,
                color = Color.Gray
            )
        }

        // Divider between items
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color(0xFFEEEEEE),
            thickness = 1.dp
        )
    }
}