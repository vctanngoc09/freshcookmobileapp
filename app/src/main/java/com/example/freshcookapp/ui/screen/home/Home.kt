package com.example.freshcookapp.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
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
import com.example.freshcookapp.data.sync.FirestoreHomeSync
import com.example.freshcookapp.ui.component.*
import com.example.freshcookapp.ui.theme.Cinnabar500
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.example.freshcookapp.ui.nav.Destination



@Suppress("unused")
@Composable
fun Modifier.shimmerEffect(): Modifier {
    // Logic shimmer thực tế cần animation, ở đây mình trả về Modifier gốc tạm thời
    // Bạn hãy dùng code ShimmerEffect mình gửi ở tin nhắn trước để thay thế
    return this
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navController: NavHostController,
    onFilterClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCategoryRecipes: (String, String) -> Unit,
    onRecipeDetail: (String) -> Unit,
    onSearchDetail: (String) -> Unit,
    onNotificationClick: () -> Unit = {},
) {
    ScreenContainer {
        val context = LocalContext.current
        val app = context.applicationContext as FreshCookAppRoom
        val db = remember { AppDatabase.getDatabase(app) }

        val viewModel: HomeViewModel = viewModel(
            factory = HomeViewModel.Companion.Factory(
                recipeRepo = RecipeRepository(db),
                categoryDao = db.categoryDao(),
                db = db
            )
        )

        LaunchedEffect(true) {
            FirestoreHomeSync(db.recipeDao()).start()
            viewModel.startNotificationListener()
        }

        val categories by viewModel.categories.collectAsState()
        val userName by viewModel.userName.collectAsState()
        val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
        val suggestions by viewModel.suggestedSearch.collectAsState()
        val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsState()
        val trending by viewModel.trendingRecipes.collectAsState()
        val newDishes by viewModel.newDishes.collectAsState()
        val favoriteIds by viewModel.favoriteIds.collectAsState()
        val inFlightIds by viewModel.inFlightIds.collectAsState()

        var searchText by remember { mutableStateOf("") }

        // --- 2. TRẠNG THÁI REFRESH ---
        var isRefreshing by remember { mutableStateOf(false) }
        val refreshState = rememberPullToRefreshState()
        val coroutineScope = rememberCoroutineScope()

        // Bọc nội dung trong PullToRefreshBox
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = refreshState,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    // Giả lập reload dữ liệu (Gọi hàm sync thực tế ở đây)
                    FirestoreHomeSync(db.recipeDao()).start()
                    delay(1500) // Delay giả để hiện vòng xoay
                    isRefreshing = false
                }
            }
        ) {
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

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, radius = 16.dp),
                                    onClick = onNotificationClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    if (hasUnreadNotifications) {
                                        Badge(containerColor = Color.Red, modifier = Modifier.size(8.dp))
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_notifications),
                                    contentDescription = "Notifications",
                                    tint = Cinnabar500,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
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
                        onValueChange = { newValue ->
                            searchText = newValue
                            if (newValue.isNotEmpty()) {
                                onSearchDetail(newValue)
                            }
                        },
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
                        items(categories, key = { it.id }) { category ->
                            TrendingCategoryItem(
                                category = category,
                                onClick = { onCategoryRecipes(category.id, category.name) }
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ======== XU HƯỚNG (CÓ SKELETON) ========
                item {
                    SectionHeader(title = "Xu hướng", onViewAll = { onCategoryRecipes("TRENDING", "Xu hướng") })
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // --- LOGIC HIỂN THỊ SKELETON ---
                        if (trending.isEmpty()) {
                            // Nếu đang tải hoặc chưa có dữ liệu -> Hiện 3 khung xương
                            items(3) {
                                RecipeCardSkeleton()
                            }
                        } else {
                            // Có dữ liệu -> Hiện món thật
                            items(trending, key = { it.id }) { recipe ->
                                RecipeCard(
                                    imageUrl = recipe.imageUrl,
                                    name = recipe.name,
                                    timeCook = recipe.timeCook,
                                    difficulty = recipe.difficulty ?: "Dễ",
                                    isFavorite = favoriteIds.contains(recipe.id),
                                    onFavoriteClick = { viewModel.toggleFavorite(recipe.id) },
                                    enabled = !inFlightIds.contains(recipe.id),
                                    modifier = Modifier.clickable { onRecipeDetail(recipe.id) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ====== GỢI Ý CHO BẠN ======
                if (suggestions.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Tìm kiếm gần đây", onViewAll = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("suggestions", suggestions)
                            navController.navigate(Destination.RecentlySearched)
                        })
                        Spacer(Modifier.height(8.dp))
                    }
                    items(suggestions, key = { it.keyword + "_" + it.timestamp }) { item ->
                        SuggestKeywordCard(
                            keyword = item.keyword,
                            time = item.timestamp,
                            imageUrl = item.imageUrl,
                            onClick = { onSearchDetail(item.keyword) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }

                // ======== MỚI LÊN SÓNG GẦN ĐÂY (CÓ SKELETON) ========
                item {
                    SectionHeader(title = "Món mới lên sóng gần đây", onViewAll = { onCategoryRecipes("NEW", "Món mới lên sóng gần đây") })
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // --- LOGIC HIỂN THỊ SKELETON ---
                        if (newDishes.isEmpty()) {
                            items(3) {
                                RecipeCardSkeleton()
                            }
                        } else {
                            items(newDishes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    imageUrl = recipe.imageUrl,
                                    name = recipe.name,
                                    timeCook = recipe.timeCook,
                                    difficulty = recipe.difficulty ?: "Dễ",
                                    isFavorite = favoriteIds.contains(recipe.id),
                                    onFavoriteClick = { viewModel.toggleFavorite(recipe.id) },
                                    enabled = !inFlightIds.contains(recipe.id),
                                    modifier = Modifier.clickable { onRecipeDetail(recipe.id) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // --- DEBUG OVERLAY (tắt trước khi release) ---
        Box(modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Column(modifier = Modifier
                .padding(8.dp)
                .background(Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(8.dp))
                .padding(6.dp)) {
                Text(text = "FavIds: ${favoriteIds.size}", style = MaterialTheme.typography.labelSmall)
                Text(text = favoriteIds.joinToString(", ") { it.take(6) }, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "InFlight: ${inFlightIds.size}", style = MaterialTheme.typography.labelSmall)
                Text(text = inFlightIds.joinToString(", ") { it.take(6) }, style = MaterialTheme.typography.bodySmall)
            }
        }

    }
}