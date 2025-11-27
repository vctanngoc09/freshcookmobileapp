package com.example.freshcookapp.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecipeDetailSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header ảnh to
        SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 300.dp, cornerRadius = 0.dp)

        Column(modifier = Modifier.padding(16.dp)) {
            // Tên món to
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f), height = 32.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Dòng thời gian & độ khó
            Row {
                SkeletonBox(width = 100.dp, height = 20.dp)
                Spacer(modifier = Modifier.width(16.dp))
                SkeletonBox(width = 80.dp, height = 20.dp)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Nguyên liệu (3 dòng giả)
            SkeletonBox(width = 150.dp, height = 24.dp) // Tiêu đề
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 20.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 20.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.8f), height = 20.dp)

            Spacer(modifier = Modifier.height(30.dp))

            // Tác giả
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(width = 50.dp, height = 50.dp, cornerRadius = 50.dp) // Avatar tròn
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    SkeletonBox(width = 100.dp, height = 16.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonBox(width = 140.dp, height = 20.dp)
                }
            }
        }
    }
}