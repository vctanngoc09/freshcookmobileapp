package com.example.freshcookapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freshcookapp.ui.screen.home.Home
import com.example.freshcookapp.ui.screen.splash.Splash

@Composable
fun MyAppNavgation(navController: NavHostController, modifier: Modifier = Modifier){
    NavHost(
        navController = navController,
        startDestination = Destination.Home,
        modifier = modifier
    ) {
        composable<Destination.Home> { Home() }
        composable<Destination.New> { Home() }
        composable<Destination.Favorites> { Home() }
        composable<Destination.Research> { Home() }
        composable<Destination.Profile> { Home() }
    }
}