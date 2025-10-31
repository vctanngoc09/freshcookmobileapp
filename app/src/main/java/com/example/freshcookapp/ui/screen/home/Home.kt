package com.example.freshcookapp.ui.screen.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.R
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Cinnabar300
import com.example.freshcookapp.ui.theme.Cinnabar400
import com.example.freshcookapp.ui.theme.Cinnabar500
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import com.example.freshcookapp.domain.model.Category
import com.example.freshcookapp.ui.component.NewDishItem
import com.example.freshcookapp.ui.component.RecommendedRecipeCard
import com.example.freshcookapp.ui.component.SectionHeader
import com.example.freshcookapp.ui.component.TrendingCategoryItem

@Composable
fun Home() {
    // 🔹 Dữ liệu demo
    val trendingRecipes = listOf(
        Recipe(R.drawable.img_food1, "Honey pancakes with...", "30 min", "Dễ", false),
        Recipe(R.drawable.img_food2, "Spaghetti carbonara", "25 min", "Trung bình", true),
        Recipe(R.drawable.img_food1, "Cơm chiên hải sản", "20 min", "Dễ", false),
    )

    val recommendedRecipes = listOf(
        Recipe(R.drawable.img_food1, "Sandwich with chicken and onion", "30p trước", "Dễ", false),
        Recipe(R.drawable.img_food2, "Grilled salmon with herbs", "1h trước", "Trung bình", false),
        Recipe(R.drawable.img_food1, "Pasta with creamy sauce", "2h trước", "Khó", false)
    )

// Một vài món demo để gắn vào thể loại
    val meatRecipes = listOf(
        Recipe(R.drawable.img_food1, "Thịt kho tàu", "30 min", "Dễ", false),
        Recipe(R.drawable.img_food2, "Thịt xào hành", "20 min", "Dễ", true)
    )

    val cakeRecipes = listOf(
        Recipe(R.drawable.img_food1, "Bánh flan caramel", "40 min", "Trung bình", false),
        Recipe(R.drawable.img_food2, "Bánh bông lan", "45 min", "Trung bình", false)
    )

    val soupRecipes = listOf(
        Recipe(R.drawable.img_food1, "Canh bí đỏ tôm khô", "25 min", "Dễ", false),
        Recipe(R.drawable.img_food2, "Canh gà nấm", "30 min", "Trung bình", false)
    )

// Danh sách Category cho phần “Từ khóa thịnh hành”
    val trendingCategories = listOf(
        Category(1, "thit", "Thịt", R.drawable.kw_thit, meatRecipes),
        Category(2, "banh", "Bánh", R.drawable.kw_banh, cakeRecipes),
        Category(3, "thucdon", "Thực đơn mỗi ngày", R.drawable.kw_thit, listOf()),
        Category(4, "thitkho", "Thịt kho", R.drawable.kw_banh, meatRecipes),
        Category(5, "namduiga", "Nấm đùi gà", R.drawable.kw_thit, soupRecipes),
        Category(6, "goi", "Gỏi", R.drawable.kw_banh, listOf())
    )


    var searchText by remember { mutableStateOf("") }

    // 🔹 Dùng LazyColumn thay cho Column(verticalScroll())
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
                onFilterClick = { /* bottom sheet lọc món */ },
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
                items(trendingRecipes) { recipe ->
                    RecipeCard(
                        imageRes = recipe.imageRes,
                        title = recipe.title,
                        time = recipe.time,
                        level = recipe.level,
                        isFavorite = recipe.isFavorite,
                        onFavoriteClick = { /* TODO */ }
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
                imageRes = recipe.imageRes,
                title = recipe.title,
                time = recipe.time,
                difficulty = recipe.level,
                onRemoveClick = { /* TODO */ }
            )
        }


        // --- Món mới lên sóng gần đây ---
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(title = "Món mới lên sóng gần đây")
            Spacer(modifier = Modifier.height(8.dp))

            val newDishes = listOf(
                Triple(R.drawable.img_food1, "Thịt gà xào măng", "Trần Thị Tuyết T."),
                Triple(R.drawable.img_food2, "Lẩu cháo chim bồ câu", "Bòn Bon"),
                Triple(R.drawable.img_food1, "Bánh xếp", "Huyen le Tran"),
                Triple(R.drawable.img_food2, "Bánh flan caramel", "Ngọc Mai"),
                Triple(R.drawable.img_food1, "Cơm chiên hải sản", "Hoàng Anh")
            )

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
