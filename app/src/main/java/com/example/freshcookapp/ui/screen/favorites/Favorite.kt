package com.example.freshcookapp.ui.screen.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun Favorite(
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    val viewModel = remember { FavoriteViewModel(repo) }

    val recipes by viewModel.favoriteRecipes.collectAsState()

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Yêu thích",
                    style = MaterialTheme.typography.titleLarge,
                    color = Cinnabar500,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LIST
            if (recipes.isEmpty()) {
                FavoriteEmptyState()
            } else {
                FavoriteList(
                    recipes = recipes,
                    onRecipeClick = { recipe -> onRecipeClick(recipe.id) },
                    // Truyền sự kiện xóa xuống dưới
                    onRemoveFavorite = { recipeId ->
                        viewModel.removeFromFavorites(recipeId)
                    }
                )
            }
        }
    }
}

// ===================== COMPONENT PHỤ =====================

@Composable
private fun FavoriteList(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onRemoveFavorite: (String) -> Unit // Callback mới
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(recipes) { recipe ->
            FavoriteItemCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) },
                onFavoriteClick = { onRemoveFavorite(recipe.id) } // Nối dây sự kiện
            )
        }
    }
}

@Composable
private fun FavoriteItemCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit // Callback mới
) {
    val cardBackgroundColor = Color(0xFFE3E8EF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = recipe.imageUrl ?: R.drawable.ic_launcher_background
                    ),
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // --- NÚT TIM CÓ THỂ BẤM ĐƯỢC ---
                // Dùng IconButton để tạo vùng bấm chuẩn
                IconButton(
                    onClick = onFavoriteClick, // Khi bấm sẽ gọi hàm xóa
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp) // Padding ngoài
                        .size(32.dp)   // Kích thước vùng bấm
                        .background(Color.White.copy(alpha = 0.7f), androidx.compose.foundation.shape.CircleShape) // Thêm nền trắng mờ cho dễ nhìn (tùy chọn)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove Favorite",
                        tint = Cinnabar500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = recipe.time,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = recipe.level,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// FavoriteEmptyState giữ nguyên như cũ
@Composable
private fun FavoriteEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.RestaurantMenu, "Empty", Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Chưa có món yêu thích",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            "Hãy tìm các món ăn ngon nhé!",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}