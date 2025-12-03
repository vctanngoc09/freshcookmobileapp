package com.example.freshcookapp.ui.screen.favorites

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // <-- Dùng AsyncImage
import coil.request.ImageRequest
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.FavoriteItemSkeleton
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun getFavoriteViewModel(): FavoriteViewModel {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repository = remember { RecipeRepository(db) }

    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoriteViewModel(repository) as T
        }
    }
    return viewModel(factory = factory)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Favorite(
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val viewModel = getFavoriteViewModel()
    val recipes by viewModel.favoriteRecipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Nút Back giả lập (nếu cần thiết kế giống mockup, bạn có thể thêm icon back ở đây)
                Text(
                    text = "Món yêu thích",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Cinnabar500,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(3) {
                            FavoriteItemSkeleton()
                        }
                    }
                } else if (recipes.isEmpty()) {
                    FavoriteEmptyState()
                } else {
                    FavoriteList(
                        recipes = recipes,
                        onRecipeClick = onRecipeClick,
                        onRemoveFavorite = { recipeId ->
                            viewModel.removeFromFavorites(recipeId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteList(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(recipes, key = { it.id }) { recipe -> // Thêm key để tối ưu list
            RecipeCard(
                imageUrl = recipe.imageUrl,
                name = recipe.name,
                timeCook = recipe.timeCook ?: 0,
                difficulty = recipe.difficulty,
                isFavorite = true,
                onFavoriteClick = { onRemoveFavorite(recipe.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecipeClick(recipe.id) }
            )
        }
    }
}

@Composable
private fun FavoriteEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.RestaurantMenu,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.LightGray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Chưa có món yêu thích",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Hãy thả tim các món ăn ngon để lưu vào đây nhé!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}