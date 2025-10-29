package com.example.freshcookapp.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class Destination(val label: String) {
    @Serializable
    data object Home: Destination("Trang chủ")
    @Serializable
    data object New: Destination("Thêm món")
    @Serializable
    data object Favorites: Destination("Yêu thích")

    @Serializable
    data object Research: Destination("Tìm kiếm")

    @Serializable
    data object Profile: Destination("Tài khoản")

    @Serializable
    data object Welcome

    @Serializable
    data object Register

    @Serializable
    data object Login
}
sealed class BottomNavigation(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int,
    val route: Destination
) {
    data object Home : BottomNavigation("Trang chủ",
        Icons.Filled.Home,
        Icons.Outlined.Home,
        0,
        Destination.Home
    )
    data object New : BottomNavigation("Thêm",
        Icons.Filled.Add,
        Icons.Outlined.Add,
        0,
        Destination.New
    )
    data object Favorites : BottomNavigation("Yêu thích",
        Icons.Filled.Favorite,
        Icons.Outlined.FavoriteBorder,
        0,
        Destination.Favorites
    )

    data object Research : BottomNavigation("Tìm kiếm",
        Icons.Filled.Search,
        Icons.Outlined.Search,
        0,
        Destination.Research
    )

    data object Profile : BottomNavigation("Tài khoản",
        Icons.Filled.Person,
        Icons.Outlined.Person,
        0,
        Destination.Profile
    )
}