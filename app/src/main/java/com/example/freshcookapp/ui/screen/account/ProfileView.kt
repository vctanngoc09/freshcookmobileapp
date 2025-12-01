package com.example.freshcookapp.ui.screen.account

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.ProfileSkeleton
import com.example.freshcookapp.ui.component.RecipeCard // Đảm bảo import đúng
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onFollowerClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit
) {
    val viewModel: AuthorProfileViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Load dữ liệu khi vào màn hình
    LaunchedEffect(userId) {
        viewModel.loadAuthorProfile(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if(uiState.fullName != "Đang tải...") uiState.fullName else "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = WorkSans
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cinnabar500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (uiState.fullName == "Đang tải...") {
            Box(modifier = Modifier.padding(paddingValues)) {
                ProfileSkeleton()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. INFO
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.photoUrl)
                            .placeholder(R.drawable.avatar1)
                            .error(R.drawable.avatar1)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Cinnabar500, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = WorkSans, color = Color.Black)
                    if (uiState.username.isNotBlank()) {
                        Text("@${uiState.username}", fontSize = 16.sp, color = Color.Gray, fontFamily = WorkSans)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 2. STATS (Thống kê)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Cinnabar500)
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuthorStatItem(
                            count = uiState.followerCount.toString(),
                            label = "Followers",
                            onClick = { onFollowerClick(userId) }
                        )
                        Divider(modifier = Modifier.width(1.dp).height(30.dp), color = Color.White.copy(alpha = 0.5f))

                        AuthorStatItem(count = uiState.recipeCount.toString(), label = "Món ăn") // Ko click

                        Divider(modifier = Modifier.width(1.dp).height(30.dp), color = Color.White.copy(alpha = 0.5f))

                        AuthorStatItem(
                            count = uiState.followingCount.toString(),
                            label = "Following",
                            onClick = { onFollowingClick(userId) }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. BUTTON FOLLOW (Chỉ hiện nếu xem người khác)
                if (currentUserId != null && currentUserId != userId) {
                    item {
                        Button(
                            onClick = { viewModel.toggleFollow() },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isFollowing) Color(0xFFEEEEEE) else Color.Black,
                                contentColor = if (uiState.isFollowing) Color.Black else Color.White
                            ),
                            border = if(uiState.isFollowing) null else null
                        ) {
                            Text(
                                text = if (uiState.isFollowing) "Đang theo dõi" else "Theo dõi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = WorkSans
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // 4. LIST MÓN ĂN (GRID 2 CỘT)
                item {
                    Divider(thickness = 8.dp, color = Color(0xFFF9F9F9))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Món ăn của ${uiState.fullName.substringBefore(" ")}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WorkSans,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (uiState.authorRecipes.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Chưa có món ăn nào.", color = Color.Gray, fontFamily = WorkSans)
                            }
                        }
                    }
                } else {
                    // Render Grid 2 cột
                    items(uiState.authorRecipes.chunked(2)) { rowRecipes ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (recipe in rowRecipes) {
                                Box(modifier = Modifier.weight(1f)) {
                                    RecipeCard(
                                        imageUrl = recipe.imageUrl,
                                        name = recipe.name,
                                        timeCook = recipe.timeCook,
                                        difficulty = recipe.difficulty ?: "Dễ",
                                        isFavorite = false, // Xem người khác thì ko cần tim ở đây
                                        onFavoriteClick = {},
                                        modifier = Modifier.clickable { onRecipeClick(recipe.id) }
                                    )
                                }
                            }
                            // Spacer nếu lẻ
                            if (rowRecipes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AuthorStatItem(
    count: String,
    label: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Color.White)
        Text(text = label, fontSize = 14.sp, fontFamily = WorkSans, color = Color.White.copy(alpha = 0.9f))
    }
}