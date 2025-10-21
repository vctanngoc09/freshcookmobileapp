package com.example.freshcookapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.freshcookapp.ui.nav.MyAppNavigation
import com.example.freshcookapp.ui.theme.FreshCookAppTheme

@Composable
fun FreshCookApp() {
    FreshCookAppTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // 👉 chừa vùng status bar (nơi có camera/pin)
                .navigationBarsPadding(), // 👉 chừa vùng thanh điều hướng (dưới)
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                MyAppNavigation()
            }
        }
    }
}
