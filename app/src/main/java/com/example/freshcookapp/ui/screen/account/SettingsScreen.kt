package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.ThemeMode
import com.example.freshcookapp.ui.theme.ThemeViewModel
import com.example.freshcookapp.ui.theme.WorkSans

// Đổi tên hàm thành Content để dễ hiểu là nội dung bên trong Drawer
@Composable
fun SettingsDrawerContent(
    themeViewModel: ThemeViewModel,
    onCloseClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
    onMyDishesClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    var showThemeDialog by remember { mutableStateOf(false) }


    val drawerBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp),
        drawerContainerColor = drawerBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cài đặt",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = WorkSans,
                    color = Cinnabar500
                )

                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Cinnabar500
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // MENU ITEMS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsMenuItem(
                    icon = Icons.Default.Person,
                    text = "Chỉnh sửa tài khoản",
                    textColor = textPrimary,
                    iconColor = iconTint,
                    onClick = onEditProfileClick
                )

                SettingsMenuItem(
                    icon = Icons.Default.AccessTime,
                    text = "Món vừa xem",
                    textColor = textPrimary,
                    iconColor = iconTint,
                    onClick = onRecentlyViewedClick
                )

                SettingsMenuItem(
                    icon = Icons.Default.Book,
                    text = "Món ngon của bạn",
                    textColor = textPrimary,
                    iconColor = iconTint,
                    onClick = onMyDishesClick
                )

                SettingsMenuItem(
                    icon = Icons.Default.DarkMode,
                    text = "Chế độ màn hình nền",
                    textColor = textPrimary,
                    iconColor = iconTint,
                    onClick = onTheme
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // LOGOUT BUTTON
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar500
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = WorkSans,
                    color = Color.White
                )
            }
        }
    }
}


// =====================
//  ITEM COMPONENT
// =====================

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    text: String,
    textColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = WorkSans,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}