package com.example.freshcookapp.ui.screen.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // <-- Cần import này
// IMPORT COIL ĐỂ LOAD ẢNH NÉT
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import coil.size.Precision
// Import Resources
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500

// Helper function to create the ViewModel with dependencies
@Composable
fun rememberFavoriteViewModel(): FavoriteViewModel {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    return remember { FavoriteViewModel(repo) }
}

@Composable
fun Favorite(
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    // 1. Sử dụng helper function để khởi tạo ViewModel một cách ổn định
    val viewModel = rememberFavoriteViewModel()

    val recipes by viewModel.favoriteRecipes.collectAsState()

    // 2. Thêm một State để theo dõi việc đang tải dữ liệu lần đầu
    var initialLoadComplete by remember { mutableStateOf(false) }

    // Kích hoạt khi recipes thay đổi từ rỗng sang có dữ liệu
    LaunchedEffect(recipes) {
        if (recipes.isNotEmpty() || viewModel.favoriteRecipes.value.isEmpty()) {
            initialLoadComplete = true
        }
    }

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            // HEADER (Giữ nguyên)
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
            // Thay vì dùng recipes.isEmpty, ta kiểm tra thêm trạng thái tải lần đầu
            if (recipes.isEmpty() && !initialLoadComplete) {
                // Hiển thị loading nhẹ hoặc giữ nguyên màn hình cũ
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Cinnabar500)
                }
            } else if (recipes.isEmpty()) {
                FavoriteEmptyState()
            } else {
                FavoriteList(
                    recipes = recipes,
                    onRecipeClick = { recipe -> onRecipeClick(recipe.id) },
                    onRemoveFavorite = { recipeId ->
                        viewModel.removeFromFavorites(recipeId)
                    }
                )
            }
        }
    }
}

// ... (Các Composable còn lại giữ nguyên) ...
@Composable
private fun FavoriteList(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onRemoveFavorite: (String) -> Unit
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
                onFavoriteClick = { onRemoveFavorite(recipe.id) }
            )
        }
    }
}

@Composable
private fun FavoriteItemCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
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
                    .height(180.dp)
            ) {
                // --- HIỂN THỊ ẢNH NÉT (High Quality) ---
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.imageUrl ?: R.drawable.ic_launcher_background)
                        .size(Size.ORIGINAL)
                        .precision(Precision.EXACT)
                        .crossfade(true)
                        .build()
                )

                Image(
                    painter = painter,
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Crop cho list là chuẩn đẹp
                )

                // Nút Tim
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
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
                    text = recipe.name,
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
                            recipe.timeCook.toString(),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = recipe.difficulty.toString(),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

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