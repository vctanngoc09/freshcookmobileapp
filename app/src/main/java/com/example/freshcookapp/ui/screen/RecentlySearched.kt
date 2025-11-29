package com.example.freshcookapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.freshcookapp.ui.component.CategoryRecipeCard
import com.example.freshcookapp.ui.screen.home.SuggestItem
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.data.mapper.toRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun RecentlySearchedScreen(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    val suggestions = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<SuggestItem>>("suggestions") ?: emptyList()

    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }

    var recipes by remember { mutableStateOf<List<com.example.freshcookapp.domain.model.Recipe>>(emptyList()) }

    LaunchedEffect(suggestions) {
        if (suggestions.isEmpty()) {
            recipes = emptyList()
        } else {
            // Query DB off main thread
            val entities = withContext(Dispatchers.IO) {
                suggestions.flatMap { item ->
                    // 🔥 SỬA LỖI: Gọi đúng hàm searchRecipes và dùng .first() để lấy kết quả từ Flow
                    repo.searchRecipes(item.keyword).first()
                }
            }
            // 🔥 SỬA LỖI: Thêm kiểu dữ liệu rõ ràng cho lambda
            val unique = entities.distinctBy { recipeEntity -> recipeEntity.id }.map { recipeEntity -> recipeEntity.toRecipe() }
            recipes = unique
        }
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

        // ===== EMPTY STATE =====
        if (recipes.isEmpty() && suggestions.isNotEmpty()) { // Thêm điều kiện để không hiển thị "Không có kết quả" khi đang tải
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (recipes.isEmpty() && suggestions.isEmpty()) {
             Text(
                text = "Không có lịch sử tìm kiếm",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
        }
        else if(recipes.isEmpty()){
             Text(
                text = "Không có kết quả tìm kiếm",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
        }
        else {
             // ===== LIST =====
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recipes) { recipe ->
                    CategoryRecipeCard(
                        recipe = recipe,
                        onClick = {
                            navController.navigate(com.example.freshcookapp.ui.nav.Destination.RecipeDetail(recipeId = recipe.id))
                        }
                    )
                }
            }
        }
    }
}