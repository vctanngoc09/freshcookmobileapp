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
import androidx.compose.ui.text.style.TextAlign
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
import com.google.firebase.auth.FirebaseAuth

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

    LaunchedEffect(recipeId) {
        if (recipeId != null) viewModel.loadRecipe(recipeId)
    }

    val recipeToShow by viewModel.recipe.collectAsState()

    // Lắng nghe trạng thái follow từ ViewModel
    val isFollowingAuthor by viewModel.isFollowingAuthor.collectAsState()

    if (recipeToShow == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Cinnabar500)
        }
    } else {
        RecipeDetailView(
            recipe = recipeToShow!!,
            isFollowingAuthor = isFollowingAuthor, // Truyền trạng thái vào
            onBackClick = { navController.navigateUp() },
            onFavoriteClick = {
                viewModel.toggleFavorite()
                val msg = if (!recipeToShow!!.isFavorite) "Đã thêm vào yêu thích" else "Đã bỏ yêu thích"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onAuthorClick = { authorId ->
                navController.navigate("user_profile/$authorId")
            },
            onFollowClick = {
                // Gọi hàm xử lý follow
                viewModel.toggleFollowAuthor()
            }
        )
    }
}

@Composable
private fun RecipeDetailView(
    recipe: Recipe,
    isFollowingAuthor: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollowClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        RecipeDetailContent(
            recipe = recipe,
            isFollowingAuthor = isFollowingAuthor,
            modifier = Modifier.fillMaxSize(),
            onAuthorClick = onAuthorClick,
            onFavoriteClick = onFavoriteClick,
            onFollowClick = onFollowClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)))
        )

        RecipeDetailTopBar(
            isFavorite = recipe.isFavorite,
            onBackClick = onBackClick,
            onFavoriteClick = onFavoriteClick
        )
    }
}

@Composable
private fun RecipeDetailTopBar(
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else Color.White
            )
        }
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    isFollowingAuthor: Boolean,
    modifier: Modifier = Modifier,
    onAuthorClick: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    LazyColumn(modifier = modifier) {
        item { RecipeHeader(recipe) }
        item { RecipeInfoSection(recipe) }
        item { RecipeIngredients(recipe.ingredients) }
        item { RecipeInstructions(recipe.instructions) }

        // Nút Yêu Thích
        item {
            Button(
                onClick = onFavoriteClick,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar500.copy(alpha = 0.1f),
                    contentColor = Cinnabar500
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(if(recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                Spacer(Modifier.width(8.dp))
                Text(if(recipe.isFavorite) "Đã thêm vào yêu thích" else "Thêm vào yêu thích")
            }
        }

        // --- PHẦN TÁC GIẢ ĐÃ CẬP NHẬT LOGIC FOLLOW ---
        item {
            AuthorInfoSection(
                author = recipe.author,
                isFollowing = isFollowingAuthor,
                onAuthorClick = onAuthorClick,
                onFollowClick = onFollowClick
            )
        }

        // ... (Giữ nguyên phần Comment và Related Recipes) ...
        item {
            RelatedRecipesSection(recipes = recipe.relatedRecipes)
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
fun AuthorInfoSection(
    author: Author,
    isFollowing: Boolean,
    onAuthorClick: (String) -> Unit,
    onFollowClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isMe = currentUserId == author.id

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lên sóng vào 01 tháng 9, 2025", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))

        Image(
            painter = rememberAsyncImagePainter(model = author.avatarUrl ?: R.drawable.avatar1),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable { onAuthorClick(author.id) },
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))
        Text("Lên sóng bởi", fontSize = 12.sp, color = Cinnabar500.copy(alpha = 0.5f))

        Text(
            text = author.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 4.dp).clickable { onAuthorClick(author.id) }
        )

        Spacer(Modifier.height(12.dp))

        // Logic hiển thị nút Follow:
        // Nếu không phải là tôi thì mới hiện nút Follow
        if (!isMe) {
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    // Nếu đang follow -> Màu xám, Ngược lại -> Màu đen
                    containerColor = if (isFollowing) Color.LightGray else Color.Black,
                    contentColor = if (isFollowing) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.width(150.dp)
            ) {
                Text(
                    text = if (isFollowing) "Đang theo dõi" else "Follow",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ... Các component khác (RecipeHeader, RecipeInfoSection...) giữ nguyên ...
@Composable
private fun RecipeHeader(recipe: Recipe) {
    Image(
        painter = rememberAsyncImagePainter(model = recipe.imageUrl ?: R.drawable.ic_launcher_background),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().height(300.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun RecipeInfoSection(recipe: Recipe) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(recipe.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(recipe.time, color = Color.Gray)
        }
    }
}

@Composable
private fun RecipeIngredients(ingredients: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Nguyên Liệu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        ingredients.forEach { Text("• $it", modifier = Modifier.padding(bottom = 4.dp)) }
    }
}

@Composable
private fun RecipeInstructions(steps: List<InstructionStep>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Cách làm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        steps.forEach { step ->
            Text("Bước ${step.stepNumber}", color = Cinnabar500, fontWeight = FontWeight.Bold)
            Text(step.description, modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}

@Composable
fun RelatedRecipesSection(recipes: List<RecipePreview>) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            "Các món tương tự",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { item ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .height(200.dp)
                        .clickable { /* Navigate to detail */ },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = item.imageUrl ?: R.drawable.img_food1),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .align(Alignment.BottomCenter)
                                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                        )
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}