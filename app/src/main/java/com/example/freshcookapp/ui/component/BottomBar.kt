package com.example.freshcookapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import com.example.freshcookapp.ui.nav.BottomNavigation
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun MyBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    val bottomItems = listOf(
        BottomNavigation.Home,
        BottomNavigation.New,
        BottomNavigation.Favorites,
        BottomNavigation.Search,
        BottomNavigation.Profile
    )

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    // ⭐ Màu nền theo mode
    val barColor = if (isDark)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    else
        MaterialTheme.colorScheme.surface

    // ⭐ Màu icon/text chưa chọn theo mode
    val unselectedColor = if (isDark)
        Color.LightGray.copy(alpha = 0.8f)
    else
        Color.Gray

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = barColor
    ) {
        bottomItems.forEach { item ->
            val isSelected = currentDestination?.hasRoute(item.route::class) == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = false
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount > 0) {
                                Badge { Text(item.badgeCount.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Cinnabar500,
                    selectedTextColor = Cinnabar500,
//                    indicatorColor = Cinnabar500.copy(alpha = 0.12f),
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}