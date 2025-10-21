package com.example.freshcookapp.ui.screen.splash

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun Splash(){
    Text(text = "hello123",
        color = Cinnabar500,
        style = MaterialTheme.typography.titleLarge)
}