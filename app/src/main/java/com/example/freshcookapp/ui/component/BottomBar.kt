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
        BottomNavigation.Research,
        BottomNavigation.Profile
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        containerColor = Color.White
    ) {
        bottomItems.forEach { item ->
            val isSelected = currentDestination?.hasRoute(item.route::class) == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
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
                label = { Text(item.label,
                    style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Cinnabar500,
                    selectedTextColor = Cinnabar500,
                    indicatorColor = Cinnabar500.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}


//@Composable
//fun MyBottomBar(
//    navController: NavHostController,
//    currentDestination: NavDestination?
//) {
//    val bottomItems = listOf(
//        BottomNavigation.Home,
//        BottomNavigation.New,
//        BottomNavigation.Favorites,
//        BottomNavigation.Research,
//        BottomNavigation.Profile
//    )
//
//    NavigationBar(
//        containerColor = Color.White,
//        tonalElevation = 8.dp
//    ) {
//        NavigationBarItem(
//            selected = false,
//            onClick = { navController.navigate(Routes.home) },
//            icon = { Icon(Icons.Outlined.Home, contentDescription = "Trang chủ") },
//            label = { Text("Trang chủ") }
//        )
//        NavigationBarItem(
//            selected = true,
//            onClick = { },
//            icon = { Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm") },
//            label = { Text("Tìm kiếm") },
//            colors = NavigationBarItemDefaults.colors(
//                selectedIconColor = Cinnabar500,
//                selectedTextColor = Cinnabar500,
//                indicatorColor = Cinnabar500.copy(alpha = 0.1f)
//            )
//        )
//        NavigationBarItem(
//            selected = false,
//            onClick = { navController.navigate(Routes.newCook) },
//            icon = { Icon(Icons.Filled.Add, contentDescription = "Thêm") },
//            label = { Text("Thêm") }
//        )
//        NavigationBarItem(
//            selected = false,
//            onClick = { navController.navigate(Routes.favorite) },
//            icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Yêu thích") },
//            label = { Text("Yêu thích") }
//        )
//        NavigationBarItem(
//            selected = false,
//            onClick = { navController.navigate(Routes.profile) },
//            icon = { Icon(Icons.Outlined.Person, contentDescription = "Tài khoản") },
//            label = { Text("Tài khoản") }
//        )
//    }
//}

//@Composable
//fun CustomBottomBar(
//    navController: NavHostController,
//    currentDestination: NavDestination?
//) {
//    val bottomItems = listOf(
//        BottomNavigation.Home,
//        BottomNavigation.New,
//        BottomNavigation.Favorites,
//        BottomNavigation.Research,
//        BottomNavigation.Profile
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 10.dp)
//            .background(Color.White)
//            .padding(vertical = 12.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceAround,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//
//            bottomItems.forEach { item ->
//                val isSelected = currentDestination?.hasRoute(item.route::class) == true
//
//                Column(
//                    modifier = Modifier
//                        .clickable {
//                            if (!isSelected) {
//                                navController.navigate(item.route) {
//                                    launchSingleTop = true
//                                    restoreState = true
//                                    popUpTo(navController.graph.startDestinationId) {
//                                        saveState = true
//                                    }
//                                }
//                            }
//                        },
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Icon(
//                        painter = painterResource(
//                            id = if (isSelected) item.selectedIcon else item.unselectedIcon
//                        ),
//                        contentDescription = item.label,
//                        modifier = Modifier.size(26.dp),
//                        tint = if (isSelected) Cinnabar500 else Color.Gray
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Text(
//                        text = item.label,
//                        fontSize = 12.sp,
//                        color = if (isSelected) Color.Black else Color.Gray
//                    )
//                }
//            }
//        }
//    }
//}
