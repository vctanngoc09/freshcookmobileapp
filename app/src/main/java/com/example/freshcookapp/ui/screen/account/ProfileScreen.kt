package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.launch

// Class này dùng chung cho cả MyDishes và Profile
data class RecipeInfo(
    val id: String = "",
    val name: String = "",
    val imageUrl: String? = null,

    // ⭐ FIX LỖI: Ánh xạ từ field 'timeCook' trong Firestore sang timeCookMinutes
    @get:PropertyName("timeCook")
    val timeCookMinutes: Int = 0,

    val userId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
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
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: ProfileViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // SettingsDrawerContent phải là Composable riêng biệt
            SettingsDrawerContent(
                onCloseClick = { scope.launch { drawerState.close() } },
                onEditProfileClick = { scope.launch { drawerState.close() }; onEditProfileClick() },
                onRecentlyViewedClick = { scope.launch { drawerState.close() }; onRecentlyViewedClick() },
                onMyDishesClick = { scope.launch { drawerState.close() }; onMyDishesClick() },
                onLogoutClick = { scope.launch { drawerState.close() }; onLogoutClick() }
            )
        }
    ) {
        ScreenContainer {
            Column(modifier = modifier.fillMaxSize().background(Color.White)) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Cinnabar500)
                    }
                    Text("Tài khoản", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Cinnabar500)

                    // ICON THÔNG BÁO CÓ DẤU CHẤM ĐỎ (BADGE)
                    IconButton(onClick = onNotificationClick) {
                        BadgedBox(
                            badge = {
                                if (hasUnreadNotifications) {
                                    Badge(containerColor = Color.Red, modifier = Modifier.size(8.dp))
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_notifications),
                                contentDescription = "Notifications",
                                tint = Cinnabar500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    // AVATAR & INFO
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

                    // THỐNG KÊ (STATS)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Cinnabar500).padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatItem(count = uiState.followerCount.toString(), label = "Follower", onClick = { onFollowerClick(uiState.uid) })
                            Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                            StatItem(count = uiState.recipeCount.toString(), label = "Món", onClick = onMyDishesClick)
                            Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                            StatItem(count = uiState.followingCount.toString(), label = "Following", onClick = { onFollowingClick(uiState.uid) })
                        }
                    }

                    // BUTTONS
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Cinnabar500,
                                    contentColor = Color.White // <--- SỬA LỖI: Chữ màu Trắng
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    "Món của tôi",
                                    fontSize = 15.sp,
                                    fontFamily = WorkSans,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
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