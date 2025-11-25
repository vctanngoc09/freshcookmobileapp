package com.example.freshcookapp.ui.screen.detail

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import coil.size.Precision
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
    navController: NavHostController,
    // Thêm callback để chuyển sang màn hình thông báo
    onNotificationClick: () -> Unit = {}
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

    // Lấy trạng thái thông báo từ ViewModel (để hiện chấm đỏ)
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsState()

    var expandedImageUrl by remember { mutableStateOf<String?>(null) }

    if (recipeToShow == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Cinnabar500)
        }
    } else {
        RecipeDetailView(
            recipe = recipeToShow!!,
            isFollowingAuthor = isFollowingAuthor,
            hasUnreadNotifications = hasUnreadNotifications, // Truyền xuống
            viewModel = viewModel,
            onBackClick = { navController.navigateUp() },
            onFavoriteClick = { viewModel.toggleFavorite() },
            onAuthorClick = { authorId -> navController.navigate("user_profile/$authorId") },
            onFollowClick = { viewModel.toggleFollowAuthor() },
            onImageClick = { url -> expandedImageUrl = url },
            onNotificationClick = onNotificationClick, // Truyền xuống
            navController = navController
        )

        if (expandedImageUrl != null) {
            FullScreenImageViewer(
                imageUrl = expandedImageUrl!!,
                onDismiss = { expandedImageUrl = null }
            )
        }
    }
}

@Composable
private fun RecipeDetailView(
    recipe: Recipe,
    isFollowingAuthor: Boolean,
    hasUnreadNotifications: Boolean,
    viewModel: RecipeDetailViewModel,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
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
            onImageClick = onImageClick,
            navController = navController
        )

        // Gradient nền đen mờ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)))
        )

        // TOP BAR NÂNG CẤP
        RecipeDetailTopBar(
            recipeName = recipe.title,
            recipeId = recipe.id,
            isFavorite = recipe.isFavorite,
            hasUnreadNotifications = hasUnreadNotifications,
            onBackClick = onBackClick,
            onFavoriteClick = onFavoriteClick,
            onNotificationClick = onNotificationClick
        )
    }
}

@Composable
private fun RecipeDetailTopBar(
    recipeName: String,
    recipeId: String,
    isFavorite: Boolean,
    hasUnreadNotifications: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Nút Back
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Cụm nút bên phải: Share -> Notification -> Favorite
        Row(verticalAlignment = Alignment.CenterVertically) {

            // 1. Nút Share
            IconButton(
                onClick = { shareRecipe(context, recipeName, recipeId) },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 2. Nút Notification (Có Badge)
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .size(40.dp)
            ) {
                // Dùng Box để tự vẽ Badge
                Box {
                    Icon(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp).align(Alignment.Center)
                    )
                    if (hasUnreadNotifications) {
                        Badge(
                            containerColor = Color.Red,
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Nút Favorite
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// Hàm chia sẻ
fun shareRecipe(context: Context, title: String, id: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Món ngon này tuyệt vời lắm: $title\nXem chi tiết tại: https://freshcook.com/recipe/$id")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ món ăn")
    context.startActivity(shareIntent)
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    isFollowingAuthor: Boolean,
    modifier: Modifier = Modifier,
    onAuthorClick: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onFollowClick: () -> Unit,
    onImageClick: (String) -> Unit,
    viewModel: RecipeDetailViewModel,
    navController: NavHostController
) {
    LazyColumn(modifier = modifier) {
        // 1. Ảnh bìa
        item {
            RecipeHeader(
                recipe = recipe,
                onImageClick = { recipe.imageUrl?.let { onImageClick(it) } }
            )
        }

        // 2. Thông tin chung
        item { RecipeInfoSection(recipe) }

        // 3. Nguyên liệu
        item { RecipeIngredients(recipe.ingredients) }

        // 4. Cách làm
        item {
            RecipeInstructions(
                steps = recipe.instructions,
                onImageClick = onImageClick
            )
        }

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

        // 7. Khung Bình luận (Đã xóa nút rác)
        item {
            CommentSection(viewModel)
        }

        // 8. Món tương tự
        item {
            if (recipe.relatedRecipes.isNotEmpty()) {
                RelatedRecipesSection(recipes = recipe.relatedRecipes, navController = navController)
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
private fun RecipeHeader(recipe: Recipe, onImageClick: () -> Unit) {
    val defaultImage = R.drawable.ic_launcher_background

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(recipe.imageUrl ?: defaultImage)
            .size(Size.ORIGINAL)
            .precision(Precision.EXACT)
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp, max = 500.dp) // Chiều cao linh hoạt
            .clickable { onImageClick() },
        contentScale = ContentScale.FillWidth // Quan trọng: FillWidth để không bị crop
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

// --- CẬP NHẬT: ẢNH BƯỚC NẤU FULL WIDTH ---
@Composable
private fun RecipeInstructions(steps: List<InstructionStep>, onImageClick: (String) -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Các bước làm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray)
        }

        Spacer(Modifier.height(12.dp))

        if (steps.isEmpty()) {
            Text("Đang cập nhật...", color = Color.Gray)
        } else {
            steps.forEach { step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Số thứ tự
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Nội dung bước
                        Text(
                            text = step.description,
                            fontSize = 16.sp,
                            color = Color.Black,
                            lineHeight = 24.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        // Ảnh bước (FULL CHIỀU CAO - KHÔNG CẮT)
                        if (!step.imageUrl.isNullOrEmpty()) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(step.imageUrl)
                                    .size(Size.ORIGINAL) // Lấy ảnh gốc
                                    .precision(Precision.EXACT)
                                    .crossfade(true)
                                    .build()
                            )

                            Image(
                                painter = painter,
                                contentDescription = "Step image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight() // Chiều cao tự dãn theo ảnh
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                                    .clickable { onImageClick(step.imageUrl) },
                                contentScale = ContentScale.FillWidth // Dãn hết chiều ngang
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Khoảng cách giữa các bước
            }
        }
    }
}

@Composable
fun FullScreenImageViewer(imageUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(Size.ORIGINAL)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = "Full Screen Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 3f)
                            offset += pan
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
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
        val defaultAvatar = R.drawable.ic_launcher_background

        Image(
            painter = rememberAsyncImagePainter(model = author.avatarUrl ?: defaultAvatar),
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

@Composable
fun CommentSection(viewModel: RecipeDetailViewModel) {
    val comments by viewModel.comments.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bình luận", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

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

        if (comments.isEmpty()) {
            Text("Chưa có bình luận nào. Hãy là người đầu tiên!", color = Color.Gray, fontSize = 14.sp)
        } else {
            Column {
                comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        isOwner = comment.userId == currentUser?.uid,
                        onDelete = { viewModel.deleteComment(comment.id) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, isOwner: Boolean, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.userName.firstOrNull()?.toString() ?: "U", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = comment.timestamp?.let { java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(it) } ?: "",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            Text(comment.text, fontSize = 14.sp)
        }

        if (isOwner) {
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun RelatedRecipesSection(recipes: List<RecipePreview>, navController: NavHostController) {
    val defaultImage = R.drawable.ic_launcher_background

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
                    modifier = Modifier.width(160.dp).height(200.dp).clickable {
                        navController.navigate("recipe_detail/${item.id}")
                    },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = item.imageUrl ?: defaultImage),
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