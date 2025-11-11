package com.example.freshcookapp.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Black
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun ForgotPassword(
    onBackClick: () -> Unit,
    onSendClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Quên mật khẩu?",
                style = MaterialTheme.typography.titleLarge.copy(color = Cinnabar500),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Đừng lo lắng, hãy nhập email của bạn và chúng tôi sẽ gửi cho bạn một liên kết để đặt lại mật khẩu của bạn.",
                style = MaterialTheme.typography.bodyMedium,
                color = Black,
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Email
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Email",
                style = MaterialTheme.typography.bodyMedium,
                color = Black,
                textAlign = TextAlign.Left
            )
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "example@gmail.com"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            PrimaryButton(
                text = "Gửi",
                onClick = onSendClick
            )
        }
    }
}