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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.ui.component.NewDishItem
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.component.RecommendedRecipeCard
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.component.SectionHeader
import com.example.freshcookapp.ui.component.TrendingCategoryItem
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.screen.home.HomeViewModel.Companion.Factory


@Composable
fun Home(onFilterClick: () -> Unit, onEditProfileClick: () -> Unit, onCategoryRecipes: (String, String) -> Unit, onRecipeDetail: (String) -> Unit) {

    ScreenContainer {

        val context = LocalContext.current
        val app = context.applicationContext as FreshCookAppRoom
        val db = remember { AppDatabase.getDatabase(app) }

        val viewModel: HomeViewModel = viewModel(
            factory = HomeViewModel.Companion.Factory(
                recipeRepo = RecipeRepository(db),
                categoryDao = db.categoryDao()
            )

        )

        val categories by viewModel.categories.collectAsState()
        val userName by viewModel.userName.collectAsState()
        val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()

        var searchText by remember { mutableStateOf("") }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {

            // -------- HEADER --------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onEditProfileClick() }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(userPhotoUrl ?: R.drawable.avatar1),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Cinnabar500, CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Hi, ${userName ?: "User"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Cinnabar500,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notifications),
                            contentDescription = null,
                            tint = Cinnabar500
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Hôm nay bạn muốn\nnấu món gì?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cinnabar500,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(12.dp))

                SearchBar(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = "Tìm món ăn...",
                    onFilterClick = onFilterClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // -------- CATEGORY GRID --------
            item {
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Danh mục món ăn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Cập nhật hôm nay",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(12.dp))

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
                        TrendingCategoryItem(
                            category = category,
                            onClick = {
                                onCategoryRecipes(category.id, category.name)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            // ======== XU HƯỚNG ========
            item {
                val trending by viewModel.trendingRecipes.collectAsState()

                SectionHeader(title = "Xu hướng")
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(trending) { recipe ->

                        RecipeCard(
                            imageUrl = recipe.imageUrl,
                            name = recipe.name,
                            timeCook = recipe.timeCook,
                            difficulty = recipe.difficulty ?: "Dễ",
                            isFavorite = recipe.isFavorite,

                            onFavoriteClick = {
                                viewModel.toggleFavorite(recipe.id)
                            },
                            modifier = Modifier.clickable {
                                onRecipeDetail(recipe.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

        }
    }
}