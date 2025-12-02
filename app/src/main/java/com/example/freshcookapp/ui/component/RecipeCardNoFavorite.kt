package com.example.freshcookapp.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.ui.theme.Cinnabar50
import com.example.freshcookapp.ui.theme.Cinnabar500
import coil.compose.AsyncImage

@Composable
fun RecipeCardNoFavorite(
    imageUrl: String?,
    name: String,
    timeCook: Int,
    difficulty: String?,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val cardBackground = if (isDark)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    else
        Cinnabar50

    val borderColor = if (isDark)
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    else
        Color(0x22000000)

    val textColorPrimary = MaterialTheme.colorScheme.onSurface
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, borderColor, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {

        Column {

            // Ảnh món ăn
            AsyncImage(
                model = imageUrl.orEmpty(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(115.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            // Phần nội dung
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackground)
                    .padding(8.dp)
            ) {

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = textColorPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = textColorSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$timeCook phút",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColorSecondary
                        )
                    }

                    Text(
                        text = difficulty.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColorSecondary
                    )
                }
            }
        }
    }
}