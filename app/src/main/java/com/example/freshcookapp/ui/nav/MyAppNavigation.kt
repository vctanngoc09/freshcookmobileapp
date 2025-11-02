package com.example.freshcookapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.freshcookapp.ui.screen.auth.Login
import com.example.freshcookapp.ui.screen.auth.Register
import com.example.freshcookapp.ui.screen.auth.Welcome
import com.example.freshcookapp.ui.screen.home.Home
import com.example.freshcookapp.ui.screen.splash.Splash
import com.example.freshcookapp.ui.screen.account.ProfileScreen
import com.example.freshcookapp.ui.screen.account.NotificationScreen
import com.example.freshcookapp.ui.screen.account.EditProfileScreen
import com.example.freshcookapp.ui.screen.account.MyDishesScreen
import com.example.freshcookapp.ui.screen.account.RecentlyViewedScreen
import com.example.freshcookapp.ui.screen.account.SettingsScreen
import com.example.freshcookapp.ui.screen.account.FollowScreen
import com.example.freshcookapp.ui.screen.detail.RecipeDetail
import com.example.freshcookapp.ui.screen.favorites.Favorite
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.freshcookapp.R
import com.example.freshcookapp.domain.model.DemoData
import com.example.freshcookapp.domain.model.User
import com.example.freshcookapp.ui.screen.account.UserProfile
import com.example.freshcookapp.ui.screen.account.UserProfileRoute
import com.example.freshcookapp.ui.screen.newcook.NewCook
import com.example.freshcookapp.ui.screen.search.Search

@Composable
fun MyAppNavgation(navController: NavHostController, modifier: Modifier = Modifier){
    NavHost(
        navController = navController,
        startDestination = Destination.Home,
        modifier = modifier
    ) {
        composable<Destination.Home> { Home() }
        composable<Destination.New> { NewCook(onBackClick = {navController.navigateUp()} ) }
        composable<Destination.Favorites> { Favorite(navController = navController) }

        composable<Destination.RecipeDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.RecipeDetail>()
            RecipeDetail(
                recipeId = args.recipeId,
                navController = navController
            )
        }

        composable(
            route = "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId == null) {
                navController.popBackStack()
                return@composable
            }
            UserProfileRoute(
                userId = userId,
                navController = navController
            )
        }

        composable<Destination.Search> {
            Search(onBackClick = {navController.navigateUp()} )}
        composable<Destination.Profile> {
            ProfileScreen(
                onNotificationClick = { navController.navigate(Destination.Notification) },
                onMyDishesClick = { navController.navigate(Destination.MyDishes) },
                onRecentlyViewedClick = { navController.navigate(Destination.RecentlyViewed) },
                onEditProfileClick = { navController.navigate(Destination.EditProfile) },
                onChangePasswordClick = { /* TODO: Navigate to change password */ },
                onLogoutClick = { navController.navigate(Destination.Welcome) },
                onMenuClick = { navController.navigate(Destination.Settings) },
                onFollowerClick = { navController.navigate(Destination.Follow) },
                onFollowingClick = { navController.navigate(Destination.Follow) }
            )
        }

        // Account related screens
        composable<Destination.Follow> {
            FollowScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Destination.Settings> {
            SettingsScreen(
                onBackClick = { navController.navigateUp() },
                onEditProfileClick = { navController.navigate(Destination.EditProfile) },
                onRecentlyViewedClick = { navController.navigate(Destination.RecentlyViewed) },
                onMyDishesClick = { navController.navigate(Destination.MyDishes) },
                onLogoutClick = {
                    navController.navigate(Destination.Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Destination.Notification> {
            NotificationScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Destination.EditProfile> {
            EditProfileScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Destination.MyDishes> {
            MyDishesScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Destination.RecentlyViewed> {
            RecentlyViewedScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Auth screens
        composable<Destination.Welcome> { Welcome(onRegister = {navController.navigate(Destination.Register)},onLogin = {navController.navigate(Destination.Login)}) }

        composable<Destination.Register> { Register(onRegisterClick = {navController.navigate(Destination.Register)}, onBackClick = {navController.navigateUp()}, onLoginClick = {navController.navigate(Destination.Login)}) }

        composable<Destination.Login> { Login(onBackClick = {navController.navigateUp()}, onLoginClick = {navController.navigate(
            Destination.Home)}, onRegisterClick = {navController.navigate(Destination.Register)}, onForgotPassClick = {navController.navigate(
            Destination.Home)}) }
    }
}