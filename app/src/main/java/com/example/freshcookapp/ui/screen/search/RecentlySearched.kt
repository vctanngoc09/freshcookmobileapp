package com.example.freshcookapp.ui.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.freshcookapp.ui.component.CategoryRecipeCard
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.data.mapper.toRecipe
import com.example.freshcookapp.data.repository.SearchRepository
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.shimmerEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
// Import Destination
import com.example.freshcookapp.ui.nav.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlySearchedScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onSearchDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }

    val viewModel: RecentlySearchedViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecentlySearchedViewModel(
                    searchRepo = SearchRepository(db.recipeDao(), db.searchHistoryDao()),
                    recipeRepo = RecipeRepository(db)
                ) as T
            }
        }
    )

    val history by viewModel.fullHistory.collectAsState()

    // Convert SuggestItem → Recipe (lấy từ search local hoặc từ DB)
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }

    LaunchedEffect(history) {
        val repo = RecipeRepository(db)
        val result = history.flatMap { item ->
            repo.searchRecipes(item.keyword).first().map { it.toRecipe() }
        }.distinctBy { it.id }

        recipes = result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ===== Header =====
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = Cinnabar500,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBackClick() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Tìm kiếm gần đây",
                style = MaterialTheme.typography.titleLarge,
                color = Cinnabar500
            )
        }

        Spacer(Modifier.height(16.dp))

        when {
            history.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có lịch sử tìm kiếm", color = Color.Gray)
                }
            }

            recipes.isEmpty() -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(5) {     // Hiển thị 5 skeleton
                        CategoryRecipeCardSkeleton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(recipes) { recipe ->
                        CategoryRecipeCard(
                            recipe = recipe,
                            onClick = {
                                navController.navigate(
                                    Destination.RecipeDetail(recipeId = recipe.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRecipeCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp) // cao tương tự Card thật
            .clip(RoundedCornerShape(12.dp))
            .shimmerEffect()
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) { }
}
