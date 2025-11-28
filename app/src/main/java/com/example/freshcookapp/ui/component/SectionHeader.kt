package com.example.freshcookapp.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.freshcookapp.ui.theme.Cinnabar400

@Composable
fun SectionHeader(title: String, onViewAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Xem tất cả",
            color = Cinnabar400,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(enabled = onViewAll != null) {
                onViewAll?.invoke()
            }
        )
    }
}
