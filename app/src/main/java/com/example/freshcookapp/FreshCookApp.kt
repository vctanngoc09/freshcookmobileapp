package com.example.freshcookapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.freshcookapp.ui.component.MyBottomBar

import com.example.freshcookapp.ui.component.MyTopBar
import com.example.freshcookapp.ui.nav.MyAppNavgation
import com.example.freshcookapp.ui.theme.Blue
import com.example.freshcookapp.ui.theme.FreshCookAppTheme
import com.example.freshcookapp.ui.theme.Red
import com.example.freshcookapp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreshCookApp() {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by
    navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Scaffold(
        containerColor = White,
        topBar = {
//            MyTopBar()
        },
        bottomBar = {
            MyBottomBar(navController, currentDestination)
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = White
        ) {
            MyAppNavgation(navController = navController)
        }
    }
}
