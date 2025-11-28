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
sealed class Destination(val route: String) {
    @Serializable
    data object Splash : Destination("splash")

    @Serializable
    data object Welcome : Destination("welcome")

    @Serializable
    data object Login : Destination("login")

    @Serializable
    data object Register : Destination("register")

    @Serializable
    data object ForgotPassword : Destination("forgot_password")

    @Serializable
    data object Home : Destination("home")
    @Serializable
    data object New: Destination("Thêm món")
    @Serializable
    data object Favorites: Destination("Yêu thích")

    @Serializable
    data object Profile: Destination("Tài khoản")

    @Serializable
    data object Notification: Destination("Thông báo")

    @Serializable
    data object EditProfile: Destination("Chỉnh sửa trang cá nhân")

    @Serializable
    data object MyDishes: Destination("Món của tôi")

    @Serializable
    data object RecentlyViewed: Destination("Món ăn đã xem")

    @Serializable
    data object RecentlySearched: Destination("recently_searched")

    @Serializable
    data object Settings: Destination("Quay lại")

    @Serializable
    data class SearchResult(
        val keyword: String
    ) : Destination("search_result")


    @Serializable
    data class Follow(
        val userId: String,
        val type: String
    ): Destination("follow")

    @Serializable
    data class Search(val keyword: String? = null): Destination("Tìm kiếm")

    @Serializable
    data object Filter: Destination("filter")

    @Serializable
    data class FilteredRecipes(
        val includedIngredients: List<String>,
        val excludedIngredients: List<String>,
        val difficulty: String,
        val timeCook: Float
    ) : Destination("filtered_recipes")

    @Serializable
    data class RecipeDetail(
        val recipeId: String?
    ) : Destination("Chi tiết")

    @Serializable
    data class CategoryRecipes(
        val categoryId: String,
        val categoryName: String
    ) : Destination("category_recipes")

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

    data object Search : BottomNavigation("Tìm kiếm",
        Icons.Filled.Search,
        Icons.Outlined.Search,
        0,
        Destination.Search()
    )

    data object Profile : BottomNavigation("Tài khoản",
        Icons.Filled.Person,
        Icons.Outlined.Person,
        0,
        Destination.Profile
    )
}