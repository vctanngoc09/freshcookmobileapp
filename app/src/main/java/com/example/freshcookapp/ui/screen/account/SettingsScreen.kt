package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.ThemeViewModel
import com.example.freshcookapp.ui.theme.WorkSans

// Đổi tên hàm thành Content để dễ hiểu là nội dung bên trong Drawer
@Composable
fun SettingsDrawerContent(
    themeViewModel: ThemeViewModel,
    onCloseClick: () -> Unit = {}, // Nút đóng menu
    onEditProfileClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
    onMyDishesClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Sử dụng ModalDrawerSheet để tạo giao diện chuẩn Hamburger
    ModalDrawerSheet(
        modifier = modifier.fillMaxHeight().width(300.dp), // Chiếm chiều rộng 300dp (hoặc chỉnh fillMaxWidth(0.8f))
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // HEADER CỦA MENU
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

                // Nút đóng menu (Thay cho nút Back)
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Cinnabar500
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // DANH SÁCH MENU
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsMenuItem(
                    icon = Icons.Default.Person,
                    text = "Chỉnh sửa tài khoản",
                    onClick = onEditProfileClick
                )

                SettingsMenuItem(
                    icon = Icons.Default.AccessTime,
                    text = "Món vừa xem",
                    onClick = onRecentlyViewedClick
                )

                SettingsMenuItem(
                    icon = Icons.Default.Book,
                    text = "Món ngon của bạn",
                    onClick = onMyDishesClick
                )

                SettingsMenuItem(
                    icon = Icons.Default.DarkMode,
                    text = "Chế độ tối",
                    onClick = { themeViewModel.toggleTheme() }
                )

            }

            Spacer(modifier = Modifier.weight(1f))

            // NÚT ĐĂNG XUẤT
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

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp), // Tăng padding chút cho dễ bấm
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = WorkSans,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Arrow",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}