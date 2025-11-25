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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.data.repository.CommentRepository
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
    val commentRepo = remember { CommentRepository() }
    val viewModel = remember { RecipeDetailViewModel(repo, commentRepo) }

    LaunchedEffect(recipeId) {
        if (recipeId != null) viewModel.loadRecipe(recipeId)
    }

    val recipeToShow by viewModel.recipe.collectAsState()
    val isFollowingAuthor by viewModel.isFollowingAuthor.collectAsState()

    if (recipeToShow == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Cinnabar500)
        }
    } else {
        RecipeDetailView(
            recipe = recipeToShow!!,
            isFollowingAuthor = isFollowingAuthor,
            viewModel = viewModel,
            onBackClick = { navController.navigateUp() },
            onFavoriteClick = { viewModel.toggleFavorite() },
            onAuthorClick = { authorId -> navController.navigate("user_profile/$authorId") },
            onFollowClick = { viewModel.toggleFollowAuthor() },
            navController = navController
        )
    }
}

@Composable
private fun RecipeDetailView(
    recipe: Recipe,
    isFollowingAuthor: Boolean,
    viewModel: RecipeDetailViewModel,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    navController: NavHostController
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        RecipeDetailContent(
            recipe = recipe,
            isFollowingAuthor = isFollowingAuthor,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
            onAuthorClick = onAuthorClick,
            onFavoriteClick = onFavoriteClick,
            onFollowClick = onFollowClick,
            navController = navController
        )

        // Gradient nền đen mờ
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
    onFollowClick: () -> Unit,
    viewModel: RecipeDetailViewModel,
    navController: NavHostController
) {
    LazyColumn(modifier = modifier) {
        // 1. Ảnh bìa
        item { RecipeHeader(recipe) }

        // 2. Thông tin chung
        item { RecipeInfoSection(recipe) }

        // 3. Nguyên liệu
        item { RecipeIngredients(recipe.ingredients) }

        // 4. Cách làm
        item { RecipeInstructions(recipe.instructions) }

        // 5. Nút Yêu thích
        item {
            Button(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(recipe.isFavorite) Cinnabar500.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                    contentColor = if(recipe.isFavorite) Cinnabar500 else Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (recipe.isFavorite) "Đã thích" else "Yêu thích", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(text = "${recipe.likeCount} lượt", fontSize = 14.sp, fontWeight = FontWeight.Normal)
            }
        }

        // 6. Tác giả
        item {
            AuthorInfoSection(
                author = recipe.author,
                isFollowing = isFollowingAuthor,
                onAuthorClick = onAuthorClick,
                onFollowClick = onFollowClick
            )
        }

        // 7. KHUNG BÌNH LUẬN (ĐÃ KHÔI PHỤC)
        item {
            CommentSection(viewModel)
        }

        // 8. CÁC MÓN TƯƠI NGỰA (ĐÃ KHÔI PHỤC)
        item {
            if (recipe.relatedRecipes.isNotEmpty()) {
                RelatedRecipesSection(recipes = recipe.relatedRecipes, navController = navController)
            } else {
                // Nếu không có món tương tự (do DB ít món), ẩn hoặc hiện text
                // Text("Chưa có món tương tự", modifier = Modifier.padding(16.dp), color = Color.Gray)
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

// ===================== CÁC COMPONENT CON =====================

@Composable
private fun RecipeHeader(recipe: Recipe) {
    Image(
        painter = rememberAsyncImagePainter(model = recipe.imageUrl ?: R.drawable.img_food1),
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
        if (recipe.description.isNotBlank()) {
            Text(recipe.description, fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(recipe.time, color = Color.Gray)
            Spacer(Modifier.width(16.dp))
            Text("Độ khó: ${recipe.level}", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun RecipeIngredients(ingredients: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Nguyên Liệu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (ingredients.isEmpty()) Text("Đang cập nhật...", color = Color.Gray)
        else ingredients.forEach { Text("• $it", modifier = Modifier.padding(bottom = 4.dp)) }
    }
}

@Composable
private fun RecipeInstructions(steps: List<InstructionStep>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Cách làm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (steps.isEmpty()) Text("Đang cập nhật...", color = Color.Gray)
        else steps.forEach { step ->
            Text("Bước ${step.stepNumber}", color = Cinnabar500, fontWeight = FontWeight.Bold)
            Text(step.description, modifier = Modifier.padding(bottom = 12.dp))
        }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = author.avatarUrl ?: R.drawable.avatar1),
            contentDescription = null,
            modifier = Modifier.size(60.dp).clip(CircleShape).clickable { onAuthorClick(author.id) },
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
        if (!isMe) {
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.LightGray else Color.Black,
                    contentColor = if (isFollowing) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.width(150.dp)
            ) {
                Text(if (isFollowing) "Đang theo dõi" else "Follow", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- KHUNG BÌNH LUẬN (UI Only - Chưa có logic Backend) ---
@Composable
fun CommentSection(viewModel: RecipeDetailViewModel) {
    val comments by viewModel.comments.collectAsState()
    val commentText by viewModel.commentText.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bình luận", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Input bình luận
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = commentText,
                onValueChange = { viewModel.updateCommentText(it) },
                placeholder = { Text("Viết bình luận...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.addComment() }) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = Cinnabar500)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Button thêm comment mẫu (chỉ để test)
        Button(
            onClick = { viewModel.addSampleComment() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Thêm comment mẫu (test)", fontSize = 12.sp)
        }

        // Danh sách bình luận
        if (comments.isEmpty()) {
            Text("Chưa có bình luận nào. Hãy là người đầu tiên!", color = Color.Gray, fontSize = 14.sp)
        } else {
            // Thay LazyColumn (nested) bằng Column để tránh crash khi nested scrollable
            Column {
                comments.forEach { comment ->
                    CommentItem(comment)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Avatar giả lập
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.userName.firstOrNull()?.toString() ?: "U", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(comment.text, fontSize = 14.sp)
            Text(
                text = comment.timestamp?.let { java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(it) } ?: "",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

// --- MÓN TƯƠI NGỰA ---
@Composable
fun RelatedRecipesSection(recipes: List<RecipePreview>, navController: NavHostController) {
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
                    modifier = Modifier.width(160.dp).height(200.dp).clickable { /* Navigate */ },
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
                            modifier = Modifier.fillMaxWidth().height(80.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                        )
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}