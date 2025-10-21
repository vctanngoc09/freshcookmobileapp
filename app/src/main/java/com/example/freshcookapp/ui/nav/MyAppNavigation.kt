package com.example.freshcookapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freshcookapp.ui.screen.splash.Splash

@Composable
fun MyAppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.splash, builder = {
        composable(Routes.splash) {
            Splash()
        }
    })
}