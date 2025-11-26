package com.example.freshcookapp.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar50

@Composable
fun SuggestKeywordCard(
    keyword: String,
    time: Long,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8F4F2)), // Cinnabar50 nếu bạn có màu

        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ảnh sát trái, không để padding dư
        Image(
            painter = rememberAsyncImagePainter(imageUrl ?: R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Keyword
            Text(
                text = keyword,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(5.dp))

            // Time text
            Text(
                text = convertTimestampToText(time),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun convertTimestampToText(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm dd/MM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

