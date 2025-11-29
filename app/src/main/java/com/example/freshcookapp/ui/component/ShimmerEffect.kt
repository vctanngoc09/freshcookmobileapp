package com.example.freshcookapp.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.max

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
        ),
        label = "shimmer"
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = alpha),
                Color.LightGray.copy(alpha = 0.4f),
                Color.LightGray.copy(alpha = alpha),
            )
        )
    )
}

@Composable
fun ShimmerRecipeList() {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = 160.dp
    val spacing = 16.dp
    val itemsPerRow = max(2, (screenWidth / (cardWidth + spacing)).toInt())
    
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(itemsPerRow) {
                    RecipeCardSkeleton()
                }
            }
        }
    }
}