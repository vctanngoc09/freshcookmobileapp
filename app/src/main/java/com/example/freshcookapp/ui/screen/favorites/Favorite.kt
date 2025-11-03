package com.example.freshcookapp.ui.screen.favorites

// SỬA: Thêm import đầy đủ
import androidx.navigation.NavHostController
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.domain.model.DemoData
import com.example.freshcookapp.domain.model.Recipe
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.FreshCookAppTheme
// --- KẾT THÚC IMPORT ---

// SỬA: Nhận NavController
@Composable
fun Favorite(navController: NavHostController) {
    val favoriteRecipes = DemoData.favoriteRecipes


    Favorite(
        recipes = favoriteRecipes,
        searchQuery = "",
        onSearchQueryChanged = {},
        onRecipeClick = { recipe ->
            navController.navigate(Destination.RecipeDetail(recipeId = recipe.id))
        }
    )
}

// Hàm Giao diện (Không cần sửa)
@Composable
private fun Favorite(
    recipes: List<Recipe>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    ScreenContainer {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Cinnabar500,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Yêu thích",
                    style = MaterialTheme.typography.titleLarge,
                    color = Cinnabar500
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = "Tìm kiếm",
                onFilterClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (recipes.isEmpty()) {
                FavoriteEmptyState()
            } else {
                FavoriteList(
                    recipes = recipes,
                    onRecipeClick = onRecipeClick
                )
            }
        }
    }
}

// Hàm List (Không cần sửa)
@Composable
private fun FavoriteList(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recipes) { recipe ->
            FavoriteItemCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) }
            )
        }
    }
}

// Hàm Item Card (Không cần sửa)
@Composable
private fun FavoriteItemCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Image(
                    painter = painterResource(id = recipe.imageRes),
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = recipe.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, "Cook time", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(recipe.time, color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(recipe.level, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

// Hàm Trạng thái rỗng (Không cần sửa)
@Composable
private fun FavoriteEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.RestaurantMenu, "Empty", Modifier.size(100.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Danh sách món yêu thích...", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Khi bạn thêm món ăn...", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}