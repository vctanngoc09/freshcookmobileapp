package com.example.freshcookapp.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecipeCardSkeleton() {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(end = 12.dp)
    ) {
        // Ảnh món ăn giả
        SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 160.dp, cornerRadius = 16.dp)
        Spacer(modifier = Modifier.height(8.dp))
        // Tên món giả
        SkeletonBox(width = 120.dp, height = 20.dp)
        Spacer(modifier = Modifier.height(4.dp))
        // Thời gian giả
        SkeletonBox(width = 80.dp, height = 16.dp)
    }
}