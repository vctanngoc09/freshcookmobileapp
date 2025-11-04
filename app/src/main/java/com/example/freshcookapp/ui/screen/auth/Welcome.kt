package com.example.freshcookapp.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.White
import com.example.freshcookapp.ui.theme.WorkSans

@Composable
fun Welcome(onRegister: () -> Unit, onLogin: () -> Unit, onGoogleSignInClick: () -> Unit) {
    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(250.dp)
                        .width(250.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FRESHCOOK",
                    color = Cinnabar500,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton("Tạo tài khoản", onClick = onRegister)

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton("Đăng nhập", onClick = onLogin)

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(text = "Chưa có tài khoản?")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đăng ký",
                        color = Cinnabar500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onRegister
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Hoặc")

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Image(painterResource(R.drawable.ic_facebook_logo), contentDescription = "Facebook")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onGoogleSignInClick) {
                        Image(painterResource(R.drawable.ic_google_logo), contentDescription = "Google")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = {}) {
                        Image(painterResource(R.drawable.ic_apple_logo), contentDescription = "Apple")
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
