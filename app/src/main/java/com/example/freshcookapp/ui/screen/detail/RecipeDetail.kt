package com.example.freshcookapp.ui.screen.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.data.repository.CommentRepository
import com.example.freshcookapp.domain.model.*
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.component.RecipeDetailSkeleton
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.nav.Destination
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val viewModel: RecipeDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecipeDetailViewModel(repo, commentRepo) as T
            }
        }
    )

    LaunchedEffect(recipeId) {
        if (recipeId != null) viewModel.loadRecipe(recipeId)
    }

    val recipeToShow by viewModel.recipe.collectAsState()
    val isFollowingAuthor by viewModel.isFollowingAuthor.collectAsState()
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsState()
    val comments by viewModel.comments.collectAsState()

    var expandedImageUrl by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = refreshState,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                if (recipeId != null) viewModel.loadRecipe(recipeId)
                delay(1000)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (recipeToShow == null || isRefreshing) {
            RecipeDetailSkeleton()
        } else {
            RecipeDetailView(
                recipe = recipeToShow!!,
                commentsCount = comments.size,
                isFollowingAuthor = isFollowingAuthor,
                hasUnreadNotifications = hasUnreadNotifications,
                viewModel = viewModel,
                lazyListState = lazyListState,
                scope = scope,
                onBackClick = { navController.navigateUp() },
                onFavoriteClick = { viewModel.toggleFavorite() },
                onAuthorClick = { authorId -> navController.navigate("user_profile/$authorId") },
                onFollowClick = { viewModel.toggleFollowAuthor() },
                onImageClick = { url -> expandedImageUrl = url },
                onNotificationClick = onNotificationClick,
                navController = navController,
                onRelatedFavoriteClick = { id -> viewModel.toggleRelatedFavorite(id) }
            )

            if (expandedImageUrl != null) {
                FullScreenImageViewer(
                    imageUrl = expandedImageUrl!!,
                    onDismiss = { expandedImageUrl = null }
                )
            }
        }
    }
}

@Composable
private fun RecipeDetailView(
    recipe: Recipe,
    commentsCount: Int,
    isFollowingAuthor: Boolean,
    hasUnreadNotifications: Boolean,
    viewModel: RecipeDetailViewModel,
    lazyListState: LazyListState,
    scope: CoroutineScope,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onRelatedFavoriteClick: (String) -> Unit,
    navController: NavHostController
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        RecipeDetailContent(
            recipe = recipe,
            commentsCount = commentsCount,
            isFollowingAuthor = isFollowingAuthor,
            viewModel = viewModel,
            lazyListState = lazyListState,
            scope = scope,
            modifier = Modifier.fillMaxSize(),
            onAuthorClick = onAuthorClick,
            onFavoriteClick = onFavoriteClick,
            onFollowClick = onFollowClick,
            onImageClick = onImageClick,
            navController = navController,
            onRelatedFavoriteClick = onRelatedFavoriteClick
        )
        // Gradient che mờ phần Header để nút Back dễ nhìn
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

// --- HEADER MỚI: HỖ TRỢ VIDEO VÀ ẢNH ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecipeMediaCarousel(
    imageUrl: String?,
    videoUrl: String?,
    onImageClick: () -> Unit
) {
    // Tạo list chứa các media có tồn tại. Thứ tự: Ảnh trước, Video sau (hoặc ngược lại tùy bạn)
    val mediaItems = remember(imageUrl, videoUrl) {
        val list = mutableListOf<Pair<String, String>>() // Pair<Type, Url>
        if (!imageUrl.isNullOrBlank()) list.add("image" to imageUrl)
        if (!videoUrl.isNullOrBlank()) list.add("video" to videoUrl)
        list
    }

    if (mediaItems.isEmpty()) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(350.dp),
            contentScale = ContentScale.Crop
        )
    } else {
        val pagerState = rememberPagerState(pageCount = { mediaItems.size })

        Box {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(350.dp)
            ) { page ->
                val (type, url) = mediaItems[page]
                if (type == "image") {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clickable { onImageClick() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    VideoPlayer(videoUrl = url)
                }
            }

            // Indicator (Số trang)
            if (mediaItems.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${mediaItems.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.parse(videoUrl))
                setMediaController(MediaController(ctx))
                // start() // Bỏ comment nếu muốn tự động phát
            }
        },
        modifier = Modifier.fillMaxSize().background(Color.Black)
    )
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    commentsCount: Int,
    isFollowingAuthor: Boolean,
    lazyListState: LazyListState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    onAuthorClick: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onFollowClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onRelatedFavoriteClick: (String) -> Unit,
    viewModel: RecipeDetailViewModel,
    navController: NavHostController
) {
    LazyColumn(modifier = modifier, state = lazyListState) {
        // HEADER: Sử dụng Carousel thay vì chỉ Image
        item {
            RecipeMediaCarousel(
                imageUrl = recipe.imageUrl,
                videoUrl = recipe.videoUrl, // Đã có trong Model cập nhật
                onImageClick = { recipe.imageUrl?.let { onImageClick(it) } }
            )
        }
        item {
            RecipeInfoSection(
                recipe = recipe,
                commentsCount = commentsCount,
                onFavoriteClick = onFavoriteClick,
                onCommentClick = { scope.launch { lazyListState.animateScrollToItem(4) } }
            )
        }
        item { RecipeIngredients(recipe.ingredients) }

        // INSTRUCTION: Sử dụng hàm mới hỗ trợ nhiều ảnh
        item {
            RecipeInstructions(
                steps = recipe.instructions,
                onImageClick = onImageClick
            )
        }
        item {
            AuthorInfoSection(
                author = recipe.author,
                isFollowing = isFollowingAuthor,
                onAuthorClick = onAuthorClick,
                onFollowClick = onFollowClick
            )
        }
        item { CommentSection(viewModel) }
        item {
            if (recipe.relatedRecipes.isNotEmpty()) {
                RelatedRecipesSection(
                    recipes = recipe.relatedRecipes,
                    navController = navController,
                    onFavoriteClick = onRelatedFavoriteClick
                )
            }
        }
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

// --- CẬP NHẬT: HIỂN THỊ NHIỀU ẢNH TRONG BƯỚC LÀM ---
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
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
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(8.dp))

                        // Gộp ảnh đơn và danh sách ảnh lại
                        val images = mutableListOf<String>()
                        if (!step.imageUrl.isNullOrBlank()) images.add(step.imageUrl!!)
                        images.addAll(step.imageUrls)
                        val uniqueImages = images.distinct()

                        if (uniqueImages.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uniqueImages) { imgUrl ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(imgUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Step image",
                                        modifier = Modifier
                                            .height(120.dp)
                                            .width(150.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF0F0F0))
                                            .clickable { onImageClick(imgUrl) },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... CÁC HÀM CŨ GIỮ NGUYÊN (RecipeInfoSection, RecipeIngredients, v.v...) ...

@Composable
private fun RecipeInfoSection(
    recipe: Recipe,
    commentsCount: Int,
    onFavoriteClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
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
        Spacer(Modifier.height(12.dp))
        if (recipe.description.isNotBlank()) {
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onFavoriteClick() }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (recipe.isFavorite) Cinnabar500 else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${recipe.likeCount} yêu thích",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCommentClick() }
                    .padding(4.dp)
            ) {
                Icon(Icons.Outlined.Comment, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$commentsCount bình luận",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${recipe.timeCook} phút",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecipeIngredients(ingredients: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Nguyên Liệu",
            style = MaterialTheme.typography.titleMedium,
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
fun FullScreenImageViewer(imageUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context).data(imageUrl).size(Size.ORIGINAL).build()
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(author.avatarUrl ?: defaultAvatar)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(64.dp).clip(CircleShape).clickable { onAuthorClick(author.id) },
            contentScale = ContentScale.Crop,
            placeholder = painterResource(defaultAvatar)
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
                shape = RoundedCornerShape(24.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSection(viewModel: RecipeDetailViewModel) {
    val comments by viewModel.comments.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Bình luận (${comments.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentUser?.photoUrl ?: R.drawable.ic_launcher_background)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            TextField(
                value = commentText,
                onValueChange = { viewModel.updateCommentText(it) },
                placeholder = { Text("Viết bình luận...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.addComment() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Send",
                    tint = if (commentText.isNotBlank()) Cinnabar500 else Color.Gray
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        if (comments.isEmpty()) {
            Text(
                text = "Chưa có bình luận nào. Hãy là người đầu tiên!",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                comments.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        isOwner = comment.userId == currentUser?.uid,
                        onDelete = { viewModel.deleteComment(comment.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, isOwner: Boolean, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(if (comment.userAvatar.isNullOrBlank()) R.drawable.ic_launcher_background else comment.userAvatar)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_launcher_background)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.width(8.dp))
                LiveTimeText(
                    timestamp = comment.timestamp?.time ?: 0L,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
        if (isOwner) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp).padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { shareRecipe(context, recipeName, recipeId) },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

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
                            containerColor = Cinnabar500,
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Cinnabar500 else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

fun shareRecipe(context: Context, title: String, id: String) {
    val deepLink = "https://freshcookapp-b376c.web.app/recipe/$id"
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Món '$title' này ngon lắm, xem công thức ở đây nhé: $deepLink")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ món ăn")
    context.startActivity(shareIntent)
}

fun getRelativeTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60 * 1000 -> "Vừa xong"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} phút trước"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} giờ trước"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} ngày trước"
        else -> java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
    }
}

@Composable
fun LiveTimeText(
    timestamp: Long,
    color: Color = Color.Gray,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall
) {
    var timeText by remember { mutableStateOf(getRelativeTimeAgo(timestamp)) }
    LaunchedEffect(timestamp) {
        while (true) {
            val newText = getRelativeTimeAgo(timestamp)
            if (newText != timeText) {
                timeText = newText
            }
            delay(60000L) // Cập nhật mỗi phút
        }
    }
    Text(text = timeText, color = color, style = style, fontSize = 11.sp)
}

@Composable
fun RelatedRecipesSection(
    recipes: List<RecipePreview>,
    navController: NavHostController,
    onFavoriteClick: (String) -> Unit
) {
    val ctx = LocalContext.current
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            "Các món tương tự",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { item ->
                RecipeCard(
                    imageUrl = item.imageUrl,
                    name = item.title,
                    timeCook = item.time.replace(" phút", "").toIntOrNull() ?: 0,
                    difficulty = "Dễ",
                    isFavorite = item.isFavorite,
                    onFavoriteClick = { onFavoriteClick(item.id) },
                    modifier = Modifier.clickable {
                        if (item.id.isNotBlank()) navController.navigate(Destination.RecipeDetail(recipeId = item.id))
                        else Toast.makeText(ctx, "Món ăn không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}