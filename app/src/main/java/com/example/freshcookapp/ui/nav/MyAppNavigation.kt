package com.example.freshcookapp.ui.nav

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.freshcookapp.ui.screen.search.RecentlySearchedScreen
import com.example.freshcookapp.util.FilterStore
// --- CÁC IMPORT PHẢI ĐẦY ĐỦ NHƯ DƯỚI ---
import com.example.freshcookapp.ui.screen.auth.ForgotPassword
import com.example.freshcookapp.ui.screen.auth.Login
import com.example.freshcookapp.ui.screen.auth.Register
import com.example.freshcookapp.ui.screen.auth.Welcome
import com.example.freshcookapp.ui.screen.auth.PhoneLoginScreen // Màn hình mới
import com.example.freshcookapp.ui.screen.auth.firebaseAuthWithGitHub // Hàm login Github
import com.example.freshcookapp.ui.screen.home.Home
import com.example.freshcookapp.ui.screen.splash.Splash
import com.example.freshcookapp.ui.screen.account.ProfileScreen
import com.example.freshcookapp.ui.screen.account.NotificationScreen
import com.example.freshcookapp.ui.screen.account.EditProfileScreen
import com.example.freshcookapp.ui.screen.account.MyDishes
import com.example.freshcookapp.ui.screen.account.RecentlyViewedScreen
import com.example.freshcookapp.ui.screen.account.FollowScreen
import com.example.freshcookapp.ui.screen.account.AuthorProfileScreen
import com.example.freshcookapp.ui.screen.detail.RecipeDetail
import com.example.freshcookapp.ui.screen.favorites.Favorite
import com.example.freshcookapp.ui.screen.filter.Filter
import com.example.freshcookapp.ui.screen.home.CategoryRecipesScreen
import com.example.freshcookapp.ui.screen.newcook.NewCook
import com.example.freshcookapp.ui.screen.search.Search
import com.example.freshcookapp.ui.screen.search.SearchResultScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MyAppNavgation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Destination,
    onGoogleSignInClick: () -> Unit,
    onFacebookSignInClick: () -> Unit  // Thêm Facebook callback
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Destination.Splash> {
            Splash(onGetStartedClicked = {
                navController.navigate(Destination.Welcome) {
                    popUpTo(Destination.Splash) { inclusive = true }
                }
            })
        }

        composable<Destination.Home> {
            Home(
                navController = navController,
                onFilterClick = { navController.navigate(Destination.Filter) },
                onEditProfileClick = { navController.navigate(Destination.EditProfile) },
                onCategoryRecipes = { id, name ->
                    navController.navigate(Destination.CategoryRecipes(id, name))
                },
                onRecipeDetail = {id -> navController.navigate(Destination.RecipeDetail(id))},
                onSearchDetail = { keyword ->
                    navController.navigate(Destination.Search(keyword))
                },
                onNotificationClick = { navController.navigate(Destination.Notification) }

            )
        }

        composable<Destination.New> {
            NewCook(onBackClick = { navController.navigateUp() } )
        }

        composable<Destination.Favorites> {
            Favorite(
                onBackClick = {
                    navController.navigate(Destination.Home) {
                        popUpTo(Destination.Home) { inclusive = true }
                    }
                },
                onRecipeClick = { recipeId ->
                    navController.navigate(Destination.RecipeDetail(recipeId = recipeId))
                }
            )
        }

        composable<Destination.RecipeDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.RecipeDetail>()
            RecipeDetail(
                recipeId = args.recipeId,
                navController = navController,
                onNotificationClick = {
                    navController.navigate(Destination.Notification)
                }
            )
        }

        composable<Destination.Profile> {
            ProfileScreen(
                onNotificationClick = { navController.navigate(Destination.Notification) },
                onMyDishesClick = { navController.navigate(Destination.MyDishes) },
                onRecentlyViewedClick = { navController.navigate(Destination.RecentlyViewed) },
                onEditProfileClick = { navController.navigate(Destination.EditProfile) },
                onMenuClick = { },
                onFollowerClick = { userId -> navController.navigate(Destination.Follow(userId = userId, type = "followers")) },
                onFollowingClick = { userId -> navController.navigate(Destination.Follow(userId = userId, type = "following")) },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Destination.Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AuthorProfileScreen(
                userId = userId,
                onBackClick = { navController.navigateUp() },
                onFollowerClick = { id -> navController.navigate(Destination.Follow(userId = id, type = "followers")) },
                onFollowingClick = { id -> navController.navigate(Destination.Follow(userId = id, type = "following")) },
                onRecipeClick = { recipeId ->
                    navController.navigate(Destination.RecipeDetail(recipeId = recipeId))
                }
            )
        }

        composable<Destination.Search> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.Search>()
            Search(
                keyword = args.keyword,
                onBackClick = { navController.navigate(Destination.Home) },
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
            Filter(
                onBackClick = { navController.navigateUp() },
                onApply = { included, excluded, diff, time ->
                    FilterStore.setFilters(included, excluded, diff, time)
                    navController.currentBackStackEntry?.savedStateHandle?.set("filter_included", included)
                    navController.currentBackStackEntry?.savedStateHandle?.set("filter_excluded", excluded)
                    navController.currentBackStackEntry?.savedStateHandle?.set("filter_difficulty", diff)
                    navController.currentBackStackEntry?.savedStateHandle?.set("filter_time", time)
                    navController.navigate(Destination.FilteredRecipes(included, excluded, diff, time))
                }
            )
        }

        composable<Destination.Follow> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.Follow>()
            FollowScreen(
                userId = args.userId,
                type = args.type,
                onBackClick = { navController.navigateUp() },
                onUserClick = { clickedUserId ->
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (clickedUserId == currentUserId) {
                        navController.navigate(Destination.Profile)
                    } else {
                        navController.navigate("user_profile/$clickedUserId")
                    }
                }
            )
        }

        composable<Destination.Notification> {
            NotificationScreen(
                navController = navController,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Destination.EditProfile> {
            EditProfileScreen(
                onBackClick = { navController.navigateUp() },
                onSaveClick = { navController.navigateUp() }
            )
        }

        composable<Destination.MyDishes> {
            MyDishes(
                onBackClick = { navController.navigateUp() },
                onAddNewClick = {
                    navController.navigate(Destination.New) {
                        launchSingleTop = true
                    }
                },
                onRecipeClick = { recipeId ->
                    navController.navigate(Destination.RecipeDetail(recipeId = recipeId))
                }
            )
        }

        composable<Destination.RecentlyViewed> {
            RecentlyViewedScreen(
                onBackClick = { navController.navigateUp() },
                onRecipeClick = { recipeId ->
                    navController.navigate(Destination.RecipeDetail(recipeId = recipeId))
                }
            )
        }

        composable<Destination.RecentlySearched> {
            RecentlySearchedScreen(
                navController = navController,
                onBackClick = { navController.navigateUp() },
                onSearchDetail = { keyword ->
                    navController.navigate(Destination.Search(keyword))
                }
            )
        }

        // --- AUTH SCREENS ---

        composable<Destination.Welcome> {
            val context = LocalContext.current
            Welcome(
                onRegisterClick = { navController.navigate(Destination.Register) },
                onLoginClick = { navController.navigate(Destination.Login) },
                onGoogleSignInClick = onGoogleSignInClick,

                // 🔥 Xử lý GitHub
                onGithubSignInClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        firebaseAuthWithGitHub(activity, FirebaseAuth.getInstance()) { success, msg ->
                            if (success) {
                                navController.navigate(Destination.Home) { popUpTo(0) { inclusive = true } }
                            } else {
                                Toast.makeText(context, "Lỗi GitHub: $msg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },

                // 🔥 Xử lý Phone (Chuyển sang màn hình PhoneLogin)
                onPhoneSignInClick = { navController.navigate(Destination.PhoneLogin) },

                // 🔥 Xử lý Facebook
                onFacebookSignInClick = onFacebookSignInClick
            )
        }

        // 🔥 Màn hình PhoneLogin MỚI
        composable<Destination.PhoneLogin> {
            PhoneLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destination.Home) { popUpTo(0) { inclusive = true } }
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Destination.Register> {
            val context = LocalContext.current
            Register(
                onRegisterSuccess = { navController.navigate(Destination.Login) },
                onBackClick = { navController.navigateUp() },
                onLoginClick = { navController.navigate(Destination.Login) },
                onGoogleSignInClick = onGoogleSignInClick,

                // 🔥 GitHub & Phone cho màn hình Register
                onGithubSignInClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        firebaseAuthWithGitHub(activity, FirebaseAuth.getInstance()) { success, msg ->
                            if (success) navController.navigate(Destination.Home) { popUpTo(0) { inclusive = true } }
                            else Toast.makeText(context, "Lỗi: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onPhoneSignInClick = { navController.navigate(Destination.PhoneLogin) },

                // 🔥 Facebook
                onFacebookSignInClick = onFacebookSignInClick
            )
        }

        composable<Destination.Login> {
            val context = LocalContext.current
            Login(
                onBackClick = { navController.navigateUp() },
                onLoginSuccess = { navController.navigate(Destination.Home) { popUpTo(0){ inclusive = true } } },
                onRegisterClick = { navController.navigate(Destination.Register) },
                onForgotPassClick = { navController.navigate(Destination.ForgotPassword) },
                onGoogleSignInClick = onGoogleSignInClick,

                // 🔥 GitHub & Phone cho màn hình Login
                onGithubSignInClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        firebaseAuthWithGitHub(activity, FirebaseAuth.getInstance()) { success, msg ->
                            if (success) navController.navigate(Destination.Home) { popUpTo(0) { inclusive = true } }
                            else Toast.makeText(context, "Lỗi: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onPhoneSignInClick = { navController.navigate(Destination.PhoneLogin) },

                // 🔥 Facebook
                onFacebookSignInClick = onFacebookSignInClick
            )
        }

        composable<Destination.ForgotPassword> {
            ForgotPassword(
                onBackClick = { navController.navigateUp() },
                onSendClick = { navController.navigate(Destination.Login) }
            )
        }

        composable<Destination.CategoryRecipes> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.CategoryRecipes>()
            CategoryRecipesScreen(
                navController = navController,
                categoryId = args.categoryId,
                categoryName = args.categoryName
            )
        }

        composable<Destination.FilteredRecipes> { backStackEntry ->
            val prev = navController.previousBackStackEntry
            val included = prev?.savedStateHandle?.get<List<String>>("filter_included")
                ?: backStackEntry.toRoute<Destination.FilteredRecipes>().includedIngredients
            val excluded = prev?.savedStateHandle?.get<List<String>>("filter_excluded")
                ?: backStackEntry.toRoute<Destination.FilteredRecipes>().excludedIngredients
            val difficulty = prev?.savedStateHandle?.get<String>("filter_difficulty")
                ?: backStackEntry.toRoute<Destination.FilteredRecipes>().difficulty
            val time = prev?.savedStateHandle?.get<Float>("filter_time")
                ?: backStackEntry.toRoute<Destination.FilteredRecipes>().timeCook

            val finalIncluded = included ?: FilterStore.includedIngredients
            val finalExcluded = excluded ?: FilterStore.excludedIngredients
            val finalDifficulty = difficulty ?: FilterStore.difficulty
            val finalTime = time ?: FilterStore.timeCook

            SearchResultScreen(
                includedIngredients = finalIncluded,
                excludedIngredients = finalExcluded,
                difficulty = finalDifficulty,
                timeCook = finalTime,
                onBackClick = { navController.navigateUp() },
                onRecipeClick = { recipeId ->
                    navController.navigate(Destination.RecipeDetail(recipeId = recipeId))
                }
            )
        }
    }
}