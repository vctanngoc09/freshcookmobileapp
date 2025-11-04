package com.example.freshcookapp.ui.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.FreshCookAppTheme
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Black
import com.example.freshcookapp.ui.theme.Chewy
import com.example.freshcookapp.ui.theme.Cinnabar600
import com.example.freshcookapp.ui.theme.WorkSans

@Composable
fun Splash(onGetStartedClicked: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cinnabar600
    ) {
        ScreenContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 48.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // 🌟 Logo chữ ngộ nghĩnh
                Text(
                    text = "Fresh Cook\nApp",
                    color = Color.White,
                    fontSize = 75.sp,
                    fontFamily = Chewy,
                    lineHeight = 70.sp,
                    letterSpacing = 0.2.sp,
                    textAlign = TextAlign.Center
                )


                // 🍽 Nút Get Started
                Button(
                    onClick = onGetStartedClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(15.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Bắt đầu nấu ăn thôi nào",
                        color = Black,
                        fontSize = 18.sp,
                        fontFamily = WorkSans,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}