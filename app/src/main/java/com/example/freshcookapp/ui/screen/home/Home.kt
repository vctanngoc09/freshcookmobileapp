package com.example.freshcookapp.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
// KHÔNG CÒN IMPORT DemoData NỮA
import com.example.freshcookapp.ui.component.NewDishItem
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.component.RecommendedRecipeCard
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.component.SectionHeader
import com.example.freshcookapp.ui.component.TrendingCategoryItem
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun Home(onFilterClick: () -> Unit, onEditProfileClick: () -> Unit) {
    ScreenContainer {

        var searchText by remember { mutableStateOf("") }

        val context = LocalContext.current
        val app = context.applicationContext as FreshCookAppRoom


        val db = remember { AppDatabase.getDatabase(app) }
        val repo = remember { RecipeRepository(db) } // Dùng Repo
        val categoryDao = remember { db.categoryDao() }
        val viewModel = remember { HomeViewModel(repo, categoryDao) }

        val recipes by viewModel.recipes.collectAsState()
        val categories by viewModel.categories.collectAsState()
        val recommendedRecipes by viewModel.recommendedRecipes.collectAsState() // MỚI
        val newDishes by viewModel.newDishes.collectAsState() // MỚI

        val userName by viewModel.userName.collectAsState()
        val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onEditProfileClick() }) {
                        Image(
                            painter = rememberAsyncImagePainter(userPhotoUrl ?: R.drawable.avatar1),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Cinnabar500, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hi, ${userName ?: "User"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Cinnabar500,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { /* Notification click */ },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notifications),
                            contentDescription = "Notifications",
                            tint = Cinnabar500,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hôm nay bạn muốn\nnấu món gì?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cinnabar500,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SearchBar(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = "Tìm món ăn...",
                    onFilterClick = onFilterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                )

            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Từ khóa thịnh hành",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Cập nhật 04:28",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 330.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(categories) { category ->
                        TrendingCategoryItem(category = category) {
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SectionHeader(title = "Xu hướng")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeCard(
                            imageUrl = recipe.imageUrl,
                            title = recipe.title,
                            time = recipe.time,
                            level = recipe.level,
                            isFavorite = recipe.isFavorite,
                            onFavoriteClick = {}
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SectionHeader(title = "Gợi ý cho bạn")
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(recommendedRecipes) { recipe ->
                RecommendedRecipeCard(
                    recipe = recipe,
                    onRemoveClick = { /* TODO */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(title = "Món mới lên sóng gần đây")
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newDishes) { dish ->
                        NewDishItem(
                            dish = dish,
                            onClick = { /* TODO: mở chi tiết món */ }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}