package com.example.freshcookapp.ui.screen.account

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> }
    )

    LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
                paddingValues = innerPadding,
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

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
    paddingValues: PaddingValues,
    viewModel: NotificationViewModel,
    navController: NavHostController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        items(notifications) { notification ->
            NotificationItem(
                notification = notification,
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationModel,
    viewModel: NotificationViewModel,
    navController: NavHostController
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color.White else Color(0xFFFFF9F9))
            .clickable {
                when (notification.type) {
                    "comment", "like" -> notification.targetId?.let { navController.navigate(Destination.RecipeDetail(it)) }
                    "follow" -> notification.targetId?.let { navController.navigate("user_profile/${it}") }
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = notification.userAvatar ?: R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = notification.userName, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = notification.message, fontSize = 14.sp, fontFamily = WorkSans, color = Color.DarkGray, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = notification.time, fontSize = 11.sp, fontFamily = WorkSans, color = Color.Gray)
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Xóa") },
                        onClick = {
                            viewModel.deleteNotification(notification.id)
                            showMenu = false
                        }
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = Color(0xFFEEEEEE), thickness = 1.dp)
    }
}
