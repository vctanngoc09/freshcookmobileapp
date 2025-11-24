package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans

// Class dùng chung cho cả MyDishes và Profile (chỉ khai báo 1 lần ở đây)
data class RecipeInfo(
    val id: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val timeCookMinutes: Int = 0
)

@Composable
fun ProfileScreen(
    userId: String? = null,
    onNotificationClick: () -> Unit = {},
    onMyDishesClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onFollowerClick: (String) -> Unit = {},
    onFollowingClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: ProfileViewModel = viewModel()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val recipes = emptyList<RecipeInfo>() // Chưa load món ăn ở đây để code gọn

    ScreenContainer {
        Column(modifier = modifier.fillMaxSize().background(Color.White)) {
            // HEADER: Có Menu và Thông báo
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = Cinnabar500)
                }
                Text("Tài khoản", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Cinnabar500)
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = Cinnabar500)
                }
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = uiState.photoUrl ?: R.drawable.avatar1),
                            contentDescription = "Profile",
                            modifier = Modifier.size(140.dp).clip(CircleShape).clickable { onEditProfileClick() },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uiState.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (uiState.username.isNotBlank()) Text("@${uiState.username}", fontSize = 14.sp, fontFamily = WorkSans, color = Color.Gray)
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Cinnabar500).padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(count = uiState.followerCount.toString(), label = "Follower", onClick = { onFollowerClick(uiState.uid) })
                        Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                        StatItem(count = uiState.recipeCount.toString(), label = "Món")
                        Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                        StatItem(count = uiState.followingCount.toString(), label = "Following", onClick = { onFollowingClick(uiState.uid) })
                    }
                }

                // BUTTONS RIÊNG CỦA CHỦ TÀI KHOẢN
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRecentlyViewedClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Cinnabar500),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Cinnabar500),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Xem gần đây", fontSize = 15.sp, fontFamily = WorkSans, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onMyDishesClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cinnabar500),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Món của tôi", fontSize = 15.sp, fontFamily = WorkSans, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Color.White)
        Text(label, fontSize = 14.sp, fontFamily = WorkSans, color = Color.White)
    }
}