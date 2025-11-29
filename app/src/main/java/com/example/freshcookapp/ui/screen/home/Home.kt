package com.example.freshcookapp.ui.screen.home

import android.widget.Toast
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
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
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.example.freshcookapp.ui.nav.Destination
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun CategoryItemSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1.5f)
            .shimmerEffect()
            .clip(RoundedCornerShape(12.dp))
    )
}

@Composable
fun SuggestKeywordCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .shimmerEffect(),
        verticalAlignment = Alignment.CenterVertically
    ) {}
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
        var isRefreshing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val homeSync = remember { FirestoreHomeSync(db.recipeDao()) }

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.refreshUserData()
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = rememberPullToRefreshState(),
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    try {
                        homeSync.forceRefresh()
                        viewModel.refreshUserData()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Không thể làm mới: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isRefreshing = false
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
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
                                painter = rememberAsyncImagePainter(
                                    model = userPhotoUrl,
                                    fallback = painterResource(id = R.drawable.avatar1),
                                    error = painterResource(id = R.drawable.avatar1),
                                    placeholder = painterResource(id = R.drawable.avatar1)
                                ),
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Cinnabar500, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            // --- SỬA LỖI CĂN CHỈNH BẰNG CÁCH TẮT FONT PADDING ---
                            Text(
                                text = "Hi, ${userName ?: "User"}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                                color = Cinnabar500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false),
                                    onClick = onNotificationClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = { if (hasUnreadNotifications) { Badge(containerColor = Color.Red) } }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_notifications),
                                    contentDescription = "Notifications",
                                    tint = Cinnabar500,
                                    modifier = Modifier.size(24.dp)
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
                            if (newValue.isNotEmpty()) { onSearchDetail(newValue) }
                        },
                        placeholder = "Tìm món ăn...",
                        onFilterClick = onFilterClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ... (Các phần còn lại của LazyColumn không thay đổi)
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
                        if (categories.isEmpty() || isRefreshing) {
                            items(4) {
                                CategoryItemSkeleton()
                            }
                        } else {
                            items(categories, key = { it.id }) { category ->
                                TrendingCategoryItem(
                                    category = category,
                                    onClick = { onCategoryRecipes(category.id, category.name) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ======== XU HƯỚNG ========
                item {
                    SectionHeader(title = "Xu hướng", onViewAll = { onCategoryRecipes("TRENDING", "Xu hướng") })
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (trending.isEmpty() || isRefreshing) {
                            items(3) { RecipeCardSkeleton() }
                        } else {
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

                // ====== TÌM KIẾM GẦN ĐÂY ======
                if (suggestions.isNotEmpty() || isRefreshing) {
                    item {
                        SectionHeader(title = "Tìm kiếm gần đây", onViewAll = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("suggestions", suggestions)
                            navController.navigate(Destination.RecentlySearched)
                        })
                        Spacer(Modifier.height(8.dp))
                    }
                    if (isRefreshing) {
                        items(2) {
                            SuggestKeywordCardSkeleton()
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else {
                        items(suggestions, key = { it.keyword + "_" + it.timestamp }) { item ->
                            SuggestKeywordCard(
                                keyword = item.keyword,
                                time = item.timestamp,
                                imageUrl = item.imageUrl,
                                onClick = { onSearchDetail(item.keyword) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }


                // ======== MỚI LÊN SÓNG GẦN ĐÂY ========
                item {
                    SectionHeader(title = "Món mới lên sóng gần đây", onViewAll = { onCategoryRecipes("NEW", "Món mới lên sóng gần đây") })
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (newDishes.isEmpty() || isRefreshing) {
                            items(3) { RecipeCardSkeleton() }
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
    }
}
