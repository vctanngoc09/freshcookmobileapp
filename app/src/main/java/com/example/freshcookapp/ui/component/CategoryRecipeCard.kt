package com.example.freshcookapp.ui.component


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.R
import coil.size.Size
import coil.size.Precision


@Composable
fun CategoryRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Cinnabar500.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Row(modifier = Modifier.fillMaxWidth()
            .height(IntrinsicSize.Min)) {

            // ===== LEFT CONTENT =====
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {

                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = recipe.description.take(60) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(Modifier.height(8.dp))

                // Time + servings
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${recipe.timeCook} phút", color = Color.Gray, fontSize = 12.sp)

                    Spacer(Modifier.width(14.dp))

                    Icon(
                        painter = painterResource(R.drawable.ic_people),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${recipe.people} phần ăn", color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(Modifier.height(10.dp))


                // ===== AUTHOR WITH REAL AVATAR =====
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Avatar
                    val avatarPainter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(recipe.authorAvatar.ifEmpty { R.drawable.ic_user })
                            .size(Size.ORIGINAL)
                            .precision(Precision.EXACT)
                            .crossfade(true)
                            .build()
                    )

                    Image(
                        painter = avatarPainter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(50)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(6.dp))

                    // Name
                    Text(
                        text = recipe.authorName.ifEmpty { "Bạn" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // ===== RIGHT IMAGE (HIGH QUALITY – NO PADDING) =====
            val context = LocalContext.current
            val mainImagePainter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(recipe.imageUrl ?: R.drawable.ic_launcher_background)
                    .size(Size.ORIGINAL)
                    .precision(Precision.EXACT)
                    .crossfade(true)
                    .build()
            )

            Image(
                painter = mainImagePainter,
                contentDescription = recipe.name,
                modifier = Modifier
                    .width(130.dp)
                    .height(155.dp)
                    .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
