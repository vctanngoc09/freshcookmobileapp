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
import androidx.compose.ui.draw.shadow
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
import com.example.freshcookapp.ui.nav.Destination

@Composable
fun RecipeDetail(
    recipeId: String?,
    navController: NavHostController,
    onNotificationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    val commentRepo = remember { CommentRepository() }
    // Lưu ý: Đảm bảo ViewModel được khởi tạo đúng cách (dùng Factory nếu cần thiết như ở Favorite)
    // Ở đây mình giữ nguyên cách bạn viết nếu nó đang chạy ổn
    val viewModel = remember { RecipeDetailViewModel(repo, commentRepo) }

    LaunchedEffect(recipeId) {
        if (recipeId != null) viewModel.loadRecipe(recipeId)
    }

    val recipeToShow by viewModel.recipe.collectAsState()
    val isFollowingAuthor by viewModel.isFollowingAuthor.collectAsState()
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
            hasUnreadNotifications = hasUnreadNotifications,
            viewModel = viewModel,
            onBackClick = { navController.navigateUp() },
            onFavoriteClick = { viewModel.toggleFavorite() },
            onAuthorClick = { authorId -> navController.navigate("user_profile/$authorId") },
            onFollowClick = { viewModel.toggleFollowAuthor() },
            onImageClick = { url -> expandedImageUrl = url },
            onNotificationClick = onNotificationClick,
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
    // Dùng MaterialTheme.colorScheme.background để chuẩn dark mode sau này
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

        // Gradient che phần status bar để icon Back/Share luôn rõ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
        )

        RecipeDetailTopBar(
            recipeName = recipe.name,
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
            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Share
            IconButton(
                onClick = { shareRecipe(context, recipeName, recipeId) },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Notification
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(40.dp)
            ) {
                Box {
                    Icon(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp).align(Alignment.Center)
                    )
                    if (hasUnreadNotifications) {
                        Badge(
                            containerColor = Cinnabar500, // Dùng màu theme đỏ
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Cinnabar500 else Color.White, // Đỏ theme hoặc trắng
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

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
        // 1. Header Ảnh
        item {
            RecipeHeader(
                recipe = recipe,
                onImageClick = { recipe.imageUrl?.let { onImageClick(it) } }
            )
        }

        // 2. Thông tin chính (Tên, thời gian)
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

        // 5. Nút Thích to (Call to Action)
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
                Text(
                    text = if (recipe.isFavorite) "Đã thích món này" else "Thêm vào yêu thích",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${recipe.likeCount} lượt thích",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
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

        // 7. Bình luận
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
            .heightIn(min = 280.dp, max = 400.dp) // Điều chỉnh chiều cao cho đẹp hơn
            .clickable { onImageClick() },
        contentScale = ContentScale.Crop // Crop ảnh cho vừa khung hình
    )
}

@Composable
private fun RecipeInfoSection(recipe: Recipe) {
    Column(modifier = Modifier.padding(16.dp)) {
        // SỬA: Dùng Typography theme thay vì fontSize cứng
        Text(
            text = recipe.name,
            style = MaterialTheme.typography.headlineMedium, // Font to, đậm từ Theme
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        if (recipe.description.isNotBlank()) {
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium, // Font mô tả chuẩn
                color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
        }

        // Hàng thông tin phụ
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Thời gian
            Icon(Icons.Default.Schedule, null, tint = Cinnabar500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${recipe.timeCook} phút",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )

            Spacer(Modifier.width(16.dp))

            // Độ khó (bọc trong thẻ màu nhẹ giống Favorite)
            Surface(
                color = Cinnabar500.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = recipe.difficulty ?: "Dễ",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Cinnabar500,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RecipeIngredients(ingredients: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Nguyên Liệu",
            style = MaterialTheme.typography.titleMedium, // Font tiêu đề section
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        if (ingredients.isEmpty()) {
            Text("Đang cập nhật...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        } else {
            ingredients.forEach {
                Text(
                    text = "• $it",
                    modifier = Modifier.padding(bottom = 6.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun RecipeInstructions(steps: List<InstructionStep>, onImageClick: (String) -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Các bước làm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Icon(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.LightGray)
        }

        Spacer(Modifier.height(12.dp))

        if (steps.isEmpty()) {
            Text("Đang cập nhật...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        } else {
            steps.forEach { step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Số thứ tự: Dùng màu Cinnabar500 cho nổi bật hoặc Đen (tùy bạn, ở đây để Đen cho clean)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            lineHeight = 22.sp // Tăng chiều cao dòng cho dễ đọc
                        )

                        Spacer(Modifier.height(8.dp))

                        if (!step.imageUrl.isNullOrEmpty()) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(step.imageUrl)
                                    .size(Size.ORIGINAL)
                                    .precision(Precision.EXACT)
                                    .crossfade(true)
                                    .build()
                            )

                            Image(
                                painter = painter,
                                contentDescription = "Step image",
                                modifier = Modifier
                                    .height(120.dp) // Tăng nhẹ kích thước
                                    .width(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF0F0F0))
                                    .clickable { onImageClick(step.imageUrl) },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// FullScreenImageViewer giữ nguyên (tốt rồi)
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
                contentDescription = null,
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
            modifier = Modifier.size(64.dp).clip(CircleShape).clickable { onAuthorClick(author.id) },
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Lên sóng bởi",
            style = MaterialTheme.typography.bodySmall,
            color = Cinnabar500.copy(alpha = 0.8f)
        )
        Text(
            text = author.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
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
                shape = RoundedCornerShape(24.dp), // Bo tròn hơn cho nút
                modifier = Modifier.defaultMinSize(minWidth = 140.dp).height(40.dp)
            ) {
                Text(
                    text = if (isFollowing) "Đang theo dõi" else "Theo dõi",
                    style = MaterialTheme.typography.labelLarge
                )
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
        Text("Bình luận", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = commentText,
                onValueChange = { viewModel.updateCommentText(it) },
                placeholder = { Text("Viết bình luận...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp) // Bo tròn hơn cho ô nhập
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.addComment() }) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = Cinnabar500)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (comments.isEmpty()) {
            Text("Chưa có bình luận nào. Hãy là người đầu tiên!", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            Column {
                comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        isOwner = comment.userId == currentUser?.uid,
                        onDelete = { viewModel.deleteComment(comment.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, isOwner: Boolean, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.userName.firstOrNull()?.toString() ?: "U",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = comment.timestamp?.let { java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(it) } ?: "",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }

        if (isOwner) {
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// SỬA: Cập nhật Card Món tương tự cho đồng bộ style "White Card"
@Composable
fun RelatedRecipesSection(recipes: List<RecipePreview>, navController: NavHostController) {
    val defaultImage = R.drawable.ic_launcher_background
    val ctx = LocalContext.current

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            "Các món tương tự",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Thêm vertical padding cho shadow
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { item ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .height(210.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)) // Đổ bóng
                        .clickable {
                            if (item.id.isNotBlank()) {
                                navController.navigate(Destination.RecipeDetail(recipeId = item.id))
                            } else {
                                Toast.makeText(ctx, "Món ăn không hợp lệ", Toast.LENGTH_SHORT).show()
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White), // Nền trắng
                    elevation = CardDefaults.cardElevation(0.dp) // Reset elevation mặc định để dùng shadow
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = item.imageUrl ?: defaultImage),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp) // Chiếm phần trên
                                .align(Alignment.TopCenter)
                        )

                        // Thông tin ở dưới (Nền trắng)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.White)
                                .padding(10.dp)
                                .height(80.dp) // Chiều cao phần chữ
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black,
                                maxLines = 2,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}