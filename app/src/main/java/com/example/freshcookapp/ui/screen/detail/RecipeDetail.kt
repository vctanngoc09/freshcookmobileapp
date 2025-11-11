package com.example.freshcookapp.ui.screen.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freshcookapp.R
import com.example.freshcookapp.domain.model.*
import com.example.freshcookapp.ui.nav.Destination
import com.example.freshcookapp.ui.theme.FreshCookAppTheme

@Composable
fun RecipeDetail(
    recipeId: String?,
    navController: NavHostController
) {
    val recipeToShow = DemoData.findRecipeById(recipeId) ?: DemoData.allRecipes.first()

    RecipeDetailView(
        recipe = recipeToShow,
        onBackClick = { navController.navigateUp() },
        onAuthorClick = { authorId ->
            navController.navigate("user_profile/$authorId")
        }
    )
}

@Composable
private fun RecipeDetailView(
    recipe: Recipe,
    onBackClick: () -> Unit,
    onAuthorClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        RecipeDetailContent(
            recipe = recipe,
            modifier = Modifier.fillMaxSize(),
            onAuthorClick = onAuthorClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )
        RecipeDetailTopBar(
            onBackClick = onBackClick,
            onFavoriteClick = { /* TODO */ },
            onNotifyClick = { /* TODO */ },
            onMoreClick = { /* TODO */ }
        )
    }
}

@Composable
private fun RecipeDetailTopBar(
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onNotifyClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconBackgroundColor = Color.Black.copy(alpha = 0.3f)
        val iconColor = Color.White

        IconButton(
            onClick = onBackClick,
            modifier = Modifier.background(iconBackgroundColor, CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = iconColor)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.background(iconBackgroundColor, CircleShape)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Yêu thích", tint = iconColor)
            }
            IconButton(
                onClick = onNotifyClick,
                modifier = Modifier.background(iconBackgroundColor, CircleShape)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Thông báo", tint = iconColor)
            }
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.background(iconBackgroundColor, CircleShape)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Thêm", tint = iconColor)
            }
        }
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onAuthorClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            RecipeHeader(recipe = recipe)
        }

        item {
            RecipeInfoSection(recipe = recipe)
        }

        item {
            RecipeIngredients(ingredients = recipe.ingredients)
        }

        item {
            RecipeInstructions(steps = recipe.instructions)
        }

        item {
            RecipeActionsSection()
        }

        item {
            CommentsSection(
                author = recipe.author,
                onAuthorClick = onAuthorClick
            )
        }

        item {
            RelatedRecipes(recipes = recipe.relatedRecipes)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RecipeHeader(recipe: Recipe) {
//    Image(
//        painter = painterResource(id = recipe.imageRes),
//        contentDescription = "Ảnh món ăn",
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(350.dp),
//        contentScale = ContentScale.Crop
//    )
}

@Composable
private fun RecipeInfoSection(recipe: Recipe) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            recipe.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Cooksnap",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp) // Đã sửa kích thước Icon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Gửi cooksnap đầu tiên!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            recipe.hashtags.forEach { tag ->
                Text(
                    text = tag,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = "Thời gian nấu",
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                recipe.time,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun RecipeActionsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { /* TODO: Xử lý Yêu thích */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm món ưu thích")
        }

        Text("Lưu Công thức: 123456", fontSize = 12.sp, color = Color.Gray)
        Text("Lần sóng vào 01 tháng 9, 2025", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun RecipeIngredients(ingredients: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp)) {
        Text("Nguyên Liệu", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ingredients.forEach { ingredient ->
            Text("• $ingredient", modifier = Modifier.padding(bottom = 8.dp), fontSize = 16.sp)
        }
    }
}

@Composable
private fun RecipeInstructions(steps: List<InstructionStep>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        Text("Cách làm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            steps.forEach { step ->
                InstructionStepItem(step = step)
            }
        }
    }
}

@Composable
private fun InstructionStepItem(step: InstructionStep) {
    Column {
        Text(
            "Bước ${step.stepNumber}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(step.description, fontSize = 16.sp, lineHeight = 24.sp)
        if (step.imageUrl != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = step.imageUrl),
                contentDescription = "Ảnh minh họa Bước ${step.stepNumber}",
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun CommentsSection(
    author: Author,
    onAuthorClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        Surface(
            modifier = Modifier.clickable { onAuthorClick(author.id) },
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar1),
                        contentDescription = author.name,
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(author.name.replace("Bởi ", ""), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Tác giả", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Button(onClick = { /* TODO: Xử lý Follow */ }) {
                    Text("Follow")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Bình Luận", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Thêm bình luận") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chưa có bình luận nào.", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun RelatedRecipes(recipes: List<RecipePreview>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Các món tương tự",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recipes) { recipe ->
                RelatedRecipeItem(recipe = recipe)
            }
        }
    }
}

@Composable
private fun RelatedRecipeItem(recipe: RecipePreview) {
    Card(modifier = Modifier.width(180.dp)) {
        Column {
            Image(
                painter = painterResource(id = recipe.imageUrl),
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Text(
                text = recipe.title,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}