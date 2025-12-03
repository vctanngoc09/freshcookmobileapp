package com.example.freshcookapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteItemSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Ảnh skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shimmerEffect()
        )

        Spacer(Modifier.height(12.dp))

        // Tên món skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(20.dp)
                .padding(horizontal = 12.dp)
                .shimmerEffect()
        )

        Spacer(Modifier.height(6.dp))

        // Time & difficulty skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(16.dp)
                .padding(horizontal = 12.dp)
                .shimmerEffect()
        )

        Spacer(Modifier.height(16.dp))
    }
}