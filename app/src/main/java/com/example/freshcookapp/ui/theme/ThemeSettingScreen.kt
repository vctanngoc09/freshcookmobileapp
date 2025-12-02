package com.example.freshcookapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingScreen(
    themeViewModel: ThemeViewModel,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // TOP BAR — giống hệt RecentlyViewed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Cinnabar500
                )
            }

            Text(
                text = "Chế độ màn hình",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSans,
                color = Cinnabar500,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Nội dung
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Chuyển đổi FreshCook giữa sáng và tối. Bạn cũng có thể dùng theo cài đặt thiết bị.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontFamily = WorkSans
            )

            Spacer(Modifier.height(32.dp))

            ThemeOptionItem("Theo thiết bị", ThemeMode.SYSTEM, themeViewModel)
            Divider()
            ThemeOptionItem("Sáng", ThemeMode.LIGHT, themeViewModel)
            Divider()
            ThemeOptionItem("Tối", ThemeMode.DARK, themeViewModel)
            Divider()
        }
    }
}


@Composable
private fun ThemeOptionItem(
    label: String,
    mode: ThemeMode,
    themeViewModel: ThemeViewModel
) {
    val current by themeViewModel.themeMode.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { themeViewModel.setTheme(mode) }
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 16.sp,
            fontFamily = WorkSans,
            color = MaterialTheme.colorScheme.onBackground
        )

        RadioButton(
            selected = current == mode,
            onClick = { themeViewModel.setTheme(mode) },
            colors = RadioButtonDefaults.colors(
                selectedColor = Cinnabar500
            )
        )
    }
}