package com.example.freshcookapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.freshcookapp.ui.screen.auth.ForgotPassword
import com.example.freshcookapp.ui.screen.auth.Login
import com.example.freshcookapp.ui.screen.auth.Register
import com.example.freshcookapp.ui.screen.auth.Welcome
import com.example.freshcookapp.ui.screen.home.Home
import com.example.freshcookapp.ui.screen.splash.Splash
import com.example.freshcookapp.ui.screen.account.ProfileScreen
import com.example.freshcookapp.ui.screen.account.NotificationScreen
import com.example.freshcookapp.ui.screen.account.EditProfileScreen
import com.example.freshcookapp.ui.screen.account.MyDishes
import com.example.freshcookapp.ui.screen.account.RecentlyViewedScreen
import com.example.freshcookapp.ui.screen.account.SettingsScreen
import com.example.freshcookapp.ui.screen.account.FollowScreen
import com.example.freshcookapp.ui.screen.detail.RecipeDetail
import com.example.freshcookapp.ui.screen.favorites.Favorite
import com.example.freshcookapp.domain.model.DemoData
import com.example.freshcookapp.ui.screen.filter.Filter
import com.example.freshcookapp.ui.screen.newcook.NewCook
import com.example.freshcookapp.ui.screen.search.Search
import com.example.freshcookapp.ui.screen.search.SearchResultScreen
import com.google.android.gms.appsearch.SearchResult

@Composable
fun MyAppNavgation(navController: NavHostController, modifier: Modifier = Modifier, startDestination: Destination, onGoogleSignInClick: () -> Unit){
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Destination.Splash> { Splash(onGetStartedClicked = {navController.navigate(Destination.Welcome) {
            popUpTo(Destination.Splash) { inclusive = true } // ✅ xóa Splash khỏi back stack
        }}) }
        composable<Destination.Home> { Home(onFilterClick = {navController.navigate(Destination.Filter)}, onEditProfileClick = { navController.navigate(Destination.EditProfile) }) }
        composable<Destination.New> { NewCook(onBackClick = {navController.navigateUp()} ) }
        composable<Destination.Favorites> {
            Favorite(
                // 1. Xử lý nút Back
                onBackClick = {
                    navController.navigate(Destination.Home) {
                        popUpTo(Destination.Home) { inclusive = true }
                    }
                },
                // 2. Xử lý khi bấm vào món ăn (QUAN TRỌNG)
                // Thay vì truyền navController vào trong, ta xử lý chuyển trang ngay tại đây
                onRecipeClick = { recipeId ->
                    navController.navigate(Destination.RecipeDetail(recipeId = recipeId))
                }
            )
        }


        composable<Destination.RecipeDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.RecipeDetail>()
            RecipeDetail(
                recipeId = args.recipeId,
                navController = navController
            )
        }

        composable<Destination.Profile> {
            ProfileScreen(
                onNotificationClick = { navController.navigate(Destination.Notification) },
                onMyDishesClick = { navController.navigate(Destination.MyDishes) },
                onRecentlyViewedClick = { navController.navigate(Destination.RecentlyViewed) },
                onEditProfileClick = { navController.navigate(Destination.EditProfile) },
                onMenuClick = { navController.navigate(Destination.Settings) },
                onFollowerClick = { userId -> navController.navigate(Destination.Follow(userId = userId, type = "followers")) },
                onFollowingClick = { userId -> navController.navigate(Destination.Follow(userId = userId, type = "following")) }
            )
        }

        composable<Destination.Search> {
            Search(
                onBackClick = { navController.popBackStack() },
                onFilterClick = { navController.navigate(Destination.Filter) },
                onSuggestionClick = { keyword ->
                    navController.navigate(Destination.SearchResult(keyword))
                }
            )
        }

        composable<Destination.SearchResult> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.SearchResult>()

            SearchResultScreen(
                keyword = args.keyword,
                onBackClick = { navController.navigateUp() },
                onRecipeClick = { recipeId ->
                    navController.navigate(Destination.RecipeDetail(recipeId))
                }
            )
        }




        composable<Destination.Filter> {
            Filter(onBackClick = {navController.navigateUp()}, onApply = {navController.navigate(
                Destination.Home)} )}
        

        // Account related screens
        composable<Destination.Follow> {backStackEntry ->
            val args = backStackEntry.toRoute<Destination.Follow>()
            FollowScreen(
                userId = args.userId,
                type = args.type,
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
                onBackClick = { navController.navigateUp() },
                onSaveClick = { navController.navigateUp() } // Quay lại sau khi lưu
            )
        }

        composable<Destination.MyDishes> {
            MyDishes(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Destination.RecentlyViewed> {
            RecentlyViewedScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Auth screens
        composable<Destination.Welcome> {
            Welcome(
                onRegisterClick = { navController.navigate(Destination.Register) },
                onLoginClick = { navController.navigate(Destination.Login) },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        composable<Destination.Register> {
            Register(
                onRegisterSuccess = { navController.navigate(Destination.Login) }, 
                onBackClick = { navController.navigateUp() },
                onLoginClick = { navController.navigate(Destination.Login) },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        composable<Destination.Login> {
            Login(
                onBackClick = { navController.navigateUp() },
                onLoginSuccess = { navController.navigate(Destination.Home) },
                onRegisterClick = { navController.navigate(Destination.Register) },
                onForgotPassClick = { navController.navigate(Destination.ForgotPassword) },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        composable<Destination.ForgotPassword> {
            ForgotPassword(
                onBackClick = { navController.navigateUp() },
                onSendClick = { navController.navigate(Destination.Login) }
            )
        }
    }
}
