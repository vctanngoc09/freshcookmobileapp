package com.example.freshcookapp.ui.screen.favorites

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter // Cần thêm thư viện Coil nếu chưa có
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun Favorite(
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    // 1. SETUP DỮ LIỆU THẬT
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    val viewModel = remember { FavoriteViewModel(repo) }

    // Lấy list món ăn từ ViewModel
    val recipes by viewModel.favoriteRecipes.collectAsState()

    // Quản lý tìm kiếm tại chỗ
    var searchQuery by remember { mutableStateOf("") }

    // Logic lọc tìm kiếm
    val filteredRecipes = if (searchQuery.isBlank()) {
        recipes
    } else {
        recipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back), // Đảm bảo icon này tồn tại
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

            Spacer(modifier = Modifier.height(16.dp))

            // --- SEARCH BAR ---
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Tìm kiếm trong yêu thích",
                onFilterClick = { /* Tính năng filter nâng cao sau này */ },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- LIST ---
            if (filteredRecipes.isEmpty()) {
                FavoriteEmptyState()
            } else {
                FavoriteList(
                    recipes = filteredRecipes,
                    onRecipeClick = { recipe ->
                        onRecipeClick(recipe.id)
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
    onRecipeClick: (Recipe) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp) // Để không bị che item cuối
    ) {
        items(recipes) { recipe ->
            FavoriteItemCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) }
            )
        }
    }
}

@Composable
private fun FavoriteItemCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // SỬ DỤNG COIL ĐỂ LOAD ẢNH URL
                Image(
                    painter = rememberAsyncImagePainter(
                        model = recipe.imageUrl ?: R.drawable.ic_launcher_background // Ảnh mặc định nếu null
                    ),
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Icon trái tim
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Cinnabar500, // Dùng màu đỏ của app
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Cook time",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(recipe.time, color = Color.Gray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(recipe.level, color = Color.Gray, fontSize = 14.sp)
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
            "Hãy tim các món ăn ngon nhé!",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}