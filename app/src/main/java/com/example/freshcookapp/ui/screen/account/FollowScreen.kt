package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    initialTab: Int = 0, // 0 for Follower, 1 for Following
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Cinnabar500
                )
            }

            Text(
                text = "Đang theo dõi: 28",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = WorkSans,
                color = Cinnabar500
            )
        }

        // Tab Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Follower Tab
            Button(
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) Color(0xFFECECEC) else Cinnabar500,
                    contentColor = if (selectedTab == 0) Color.Black else Color.White
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Follower",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = WorkSans
                )
            }

            // Following Tab
            Button(
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) Color(0xFFECECEC) else Cinnabar500,
                    contentColor = if (selectedTab == 1) Color.Black else Color.White
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Following",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = WorkSans
                )
            }
        }

        // Search Bar
        SearchBar(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Tìm kiếm món yêu thích",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Section Header
        Text(
            text = "Danh sách đang theo dõi",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = WorkSans,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // List of followers/following
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) { index ->
                FollowListItem(
                    name = "Maia Adam",
                    message = "liked your \"Honey pancakes\"",
                    profileImage = R.drawable.avatar1,
                    onRemoveClick = {}
                )
            }
        }
    }
}

@Composable
fun FollowListItem(
    name: String,
    message: String,
    profileImage: Int?,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile image
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Cinnabar500)
        ) {
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
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
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

        // Remove button
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Cinnabar500,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
