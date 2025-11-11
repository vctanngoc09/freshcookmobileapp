package com.example.freshcookapp.ui.screen.home

// --- TẤT CẢ CÁC IMPORT BỊ THIẾU ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.domain.model.Category
import com.example.freshcookapp.ui.component.NewDishItem
import com.example.freshcookapp.ui.component.RecommendedRecipeCard
import com.example.freshcookapp.ui.component.SectionHeader
import com.example.freshcookapp.ui.component.TrendingCategoryItem
// --- IMPORT MỚI CHO DEMODATA ---
import com.example.freshcookapp.domain.model.DemoData
import com.example.freshcookapp.ui.component.ScreenContainer

// -----------------------------------


@Composable
fun Home(onFilterClick: () -> Unit) {
    ScreenContainer {
        // 🔹 Dữ liệu demo
        // --- LẤY DỮ LIỆU TỪ DEMODATA ---
        val trendingRecipes = DemoData.trendingRecipes
        val recommendedRecipes = DemoData.recommendedRecipes
        val trendingCategories = DemoData.trendingCategories
        val newDishes = DemoData.newDishes

        var searchText by remember { mutableStateOf("") }

        val context = LocalContext.current
        val app = context.applicationContext as FreshCookAppRoom
        val repo = remember { RecipeRepository(app.database) }
        val viewModel = remember { HomeViewModel(repo) }

        val recipes by viewModel.recipes.collectAsState()

        // 🔹 Dùng LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {

            // --- Header ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.avatar1),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Cinnabar500, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hi, Vo Cao Tan Ngoc",
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
                // 🔹 Section: Từ khóa thịnh hành
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
                    items(trendingCategories) { category ->
                        TrendingCategoryItem(category = category) {
                            // TODO: navigate to category detail
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- Xu hướng ---
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

            // --- Gợi ý cho bạn ---
            item {
                SectionHeader(title = "Gợi ý cho bạn")
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recommendedRecipes) { recipe ->
                RecommendedRecipeCard(
                    imageUrl = recipe.imageUrl,
                    title = recipe.title,
                    time = recipe.time,
                    difficulty = recipe.level,
                    onRemoveClick = { /* TODO */ }
                    // Sẽ thêm clickable ở bước sau
                )
            }


            // --- Món mới lên sóng gần đây ---
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(title = "Món mới lên sóng gần đây")
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newDishes) { (image, title, author) ->
                        NewDishItem(
                            imageRes = image,
                            title = title,
                            author = author,
                            onClick = { /* TODO: mở chi tiết món */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}