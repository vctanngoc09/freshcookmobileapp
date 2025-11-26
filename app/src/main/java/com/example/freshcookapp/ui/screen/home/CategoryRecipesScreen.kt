package com.example.freshcookapp.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.ui.component.CategoryRecipeCard
import com.example.freshcookapp.ui.component.CategoryRecipeItem
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun CategoryRecipesScreen(
    navController: NavHostController,
    categoryId: String,
    categoryName: String
) {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = AppDatabase.getDatabase(app)
    val repo = RecipeRepository(db)

    val viewModel: CategoryRecipesViewModel = viewModel(
        factory = CategoryRecipesViewModel.Factory(repo)
    )

    val recipes by viewModel.recipes.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadRecipes(categoryId)
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
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Cinnabar500,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge,
                color = Cinnabar500
            )
        }

        Spacer(Modifier.height(16.dp))


        // ===== EMPTY STATE =====
        if (recipes.isEmpty()) {
            CategoryEmptyState()
            return
        }


        // ===== LIST =====
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

@Composable
fun CategoryEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Outlined.RestaurantMenu,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chưa có món nào trong danh mục này",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Text(
            text = "Hãy khám phá thêm các công thức ngon nhé!",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
    }
}
