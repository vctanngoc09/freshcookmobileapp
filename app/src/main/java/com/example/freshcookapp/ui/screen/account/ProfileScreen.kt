package com.example.freshcookapp.ui.screen.account

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.ui.component.ProfileSkeleton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.ThemeViewModel
import com.example.freshcookapp.ui.theme.WorkSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    onTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: ProfileViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }

    val activity = LocalContext.current as ComponentActivity
    val themeViewModel: ThemeViewModel = viewModel(activity)

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(repo) as T
            }
        }
    )

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

        ScreenContainer {
            Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                // HEADER
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

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    state = refreshState,
                    onRefresh = {
                        isRefreshing = true
                        scope.launch {
                            viewModel.loadProfile(userId)
                            delay(1000)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.fullName == "Đang tải...") {
                            item {
                                ProfileSkeleton()
                            }
                        } else {
                            // AVATAR & INFO
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(uiState.photoUrl)
                                            .placeholder(R.drawable.avatar1)
                                            .error(R.drawable.avatar1)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Cinnabar500, CircleShape)
                                            .clickable { onEditProfileClick() }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(uiState.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = MaterialTheme.colorScheme.onBackground)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (uiState.username.isNotBlank()) Text("@${uiState.username}", fontSize = 14.sp, fontFamily = WorkSans, color = Color.Gray)
                                }
                            }

                            // THỐNG KÊ (STATS) - ĐÃ CẬP NHẬT ĐỂ SỬA LỖI BẤM
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Cinnabar500)
                                        .padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center, // Canh giữa tổng thể
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Dùng modifier.weight(1f) để chia đều không gian bấm
                                    StatItem(
                                        count = uiState.followerCount.toString(),
                                        label = "Follower",
                                        modifier = Modifier.weight(1f),
                                        onClick = { onFollowerClick(uiState.uid) }
                                    )
                                    Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                                    StatItem(
                                        count = uiState.recipeCount.toString(),
                                        label = "Món",
                                        modifier = Modifier.weight(1f),
                                        onClick = onMyDishesClick
                                    )
                                    Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                                    StatItem(
                                        count = uiState.followingCount.toString(),
                                        label = "Following",
                                        modifier = Modifier.weight(1f),
                                        onClick = { onFollowingClick(uiState.uid) }
                                    )
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
                                            contentColor = Color.White
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

                            // ĐÃ XÓA PHẦN HIỂN THỊ DANH SÁCH MÓN ĂN Ở ĐÂY

                            // Padding cuối trang để không bị che bởi BottomBar
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

}

// Hàm StatItem hỗ trợ modifier từ bên ngoài để mở rộng vùng bấm
@Composable
fun StatItem(count: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Color.White)
        Text(label, fontSize = 14.sp, fontFamily = WorkSans, color = Color.White)
    }
}