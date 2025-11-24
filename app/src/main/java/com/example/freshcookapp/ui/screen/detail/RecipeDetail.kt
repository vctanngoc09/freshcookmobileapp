package com.example.freshcookapp.ui.screen.detail

import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.*
import com.example.freshcookapp.ui.theme.Cinnabar500

@Composable
fun RecipeDetail(
    recipeId: String?,
    navController: NavHostController
) {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    val viewModel = remember { RecipeDetailViewModel(repo) }

    // Gọi load dữ liệu khi màn hình mở lên
    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.loadRecipe(recipeId)
        }
    }

    val recipeToShow by viewModel.recipe.collectAsState()

    // Kiểm tra: Nếu đang load hoặc null thì hiện Loading
    if (recipeToShow == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Cinnabar500)
        }
    } else {
        RecipeDetailView(
            recipe = recipeToShow!!,
            onBackClick = { navController.navigateUp() },
            onFavoriteClick = {
                viewModel.toggleFavorite()
                val msg = if (!recipeToShow!!.isFavorite) "Đã thêm vào yêu thích" else "Đã bỏ yêu thích"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onAuthorClick = {
                Toast.makeText(context, "Tính năng Profile đang phát triển", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun RecipeDetailView(
    recipe: Recipe,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
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
            onAuthorClick = onAuthorClick,
            onFavoriteClick = onFavoriteClick
        )

        // Gradient mờ phần header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        RecipeDetailTopBar(
            isFavorite = recipe.isFavorite,
            onBackClick = onBackClick,
            onFavoriteClick = onFavoriteClick,
            onNotifyClick = { },
            onMoreClick = { }
        )
    }
}

@Composable
private fun RecipeDetailTopBar(
    isFavorite: Boolean,
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
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = iconColor)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.background(iconBackgroundColor, CircleShape)
            ) {
                // Đổi icon Tim đặc/Tim rỗng dựa theo trạng thái
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else iconColor
                )
            }
            // ... Các nút khác giữ nguyên ...
        }
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onAuthorClick: (String) -> Unit,
    onFavoriteClick: () -> Unit
) {
    LazyColumn(modifier = modifier) {
        item { RecipeHeader(recipe = recipe) }
        item { RecipeInfoSection(recipe = recipe) }
        item { RecipeIngredients(ingredients = recipe.ingredients) }
        item { RecipeInstructions(steps = recipe.instructions) }

        item {
            RecipeActionsSection(
                isFavorite = recipe.isFavorite,
                onFavoriteClick = onFavoriteClick
            )
        }

        item {
            CommentsSection(
                author = recipe.author,
                onAuthorClick = onAuthorClick
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun RecipeHeader(recipe: Recipe) {
    // SỬ DỤNG COIL ĐỂ LOAD ẢNH URL -> KHÔNG BỊ CRASH
    Image(
        painter = rememberAsyncImagePainter(
            model = recipe.imageUrl ?: R.drawable.ic_launcher_background
        ),
        contentDescription = "Ảnh món ăn",
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        contentScale = ContentScale.Crop
    )
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

        // ... Giữ nguyên phần icon Camera và Hashtag ...

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, "Time", tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(recipe.time, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Composable
private fun RecipeActionsSection(
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Nút thêm yêu thích to ở dưới
        OutlinedButton(
            onClick = onFavoriteClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = if (isFavorite) ButtonDefaults.outlinedButtonColors(
                containerColor = Cinnabar500.copy(alpha = 0.1f),
                contentColor = Cinnabar500
            ) else ButtonDefaults.outlinedButtonColors()
        ) {
            Icon(
                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFavorite) "Đã thêm vào yêu thích" else "Thêm món ưu thích")
        }
    }
}

// ... CÁC PHẦN CÒN LẠI (Ingredients, Instructions, Comments) GIỮ NGUYÊN CODE CŨ ...
// (Bạn chỉ cần copy phần UI hiển thị chữ từ file cũ sang là được vì logic không đổi)
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
                Column {
                    Text(
                        "Bước ${step.stepNumber}",
                        fontWeight = FontWeight.Bold,
                        color = Cinnabar500
                    )
                    Text(step.description, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun CommentsSection(author: Author, onAuthorClick: (String) -> Unit) {
    // Giữ nguyên giao diện comment như file cũ
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tác giả: ${author.name}", fontWeight = FontWeight.Bold)
    }
}