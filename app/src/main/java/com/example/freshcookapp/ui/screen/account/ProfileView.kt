package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.BorderStroke
import com.example.freshcookapp.ui.theme.Cinnabar500
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freshcookapp.R
import com.example.freshcookapp.domain.model.DemoData
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.domain.model.User
import com.example.freshcookapp.ui.theme.FreshCookAppTheme
import com.example.freshcookapp.ui.theme.WorkSans


private val allDemoUsers = listOf(
    User(
        id = DemoData.authorTanNgoc.id, // "author1"
        name = "Vo Cao Tan Ngoc", // Tên thật
        username = DemoData.authorTanNgoc.username,
        profileImage = R.drawable.avatar1, // Sửa lỗi String -> Int
        bio = "Yêu thích nấu nướng"
    ),
    User(
        id = DemoData.authorHoangAnh.id, // "author2"
        name = "Hoàng Anh", // Tên thật
        username = DemoData.authorHoangAnh.username,
        profileImage = R.drawable.avatar1, // Sửa lỗi String -> Int
        bio = "Yêu thích nấu nướng và lập trình"
    )
)


@Composable
fun UserProfileRoute(
    userId: String,
    navController: NavHostController
) {
    val user = allDemoUsers.find { it.id == userId }

    // Xử lý trường hợp không tìm thấy user
    if (user == null) {
        navController.popBackStack() // Tự động quay lại
        return
    }

    val userRecipes = DemoData.allRecipes.filter { it.author.id == userId }
    val likedRecipes = DemoData.favoriteRecipes // Tạm thời lấy danh sách yêu thích chung

    val recipeCount = userRecipes.size
    val followerCount = 23 // Tạm thời hardcode
    val followingCount = 10 // Tạm thời hardcode

    UserProfile(
        user = user,
        recipeCount = recipeCount,
        followerCount = followerCount,
        followingCount = followingCount,
        userRecipes = userRecipes,
        likedRecipes = likedRecipes,
        onBackClick = { navController.popBackStack() },
        onMoreClick = { /* TODO */ }
    )
}

@Composable
fun UserProfile(
    user: User,
    recipeCount: Int,
    followerCount: Int,
    followingCount: Int,
    userRecipes: List<Recipe>,
    likedRecipes: List<Recipe>,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFollowing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ==== Header (thay cho TopAppBar) ====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Nút back
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // Tên username ở giữa
            Text(
                text = user.username,
                fontFamily = WorkSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            // Nút more
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = Color.Black
                )
            }
        }

        // ==== Phần nội dung chính ====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Phần Header (ảnh đại diện, thống kê)
            ProfileHeader(
                user = user,
                recipeCount = recipeCount,
                followerCount = followerCount,
                followingCount = followingCount
            )

            // Nút Follow / Share
            ProfileActions(
                isFollowing = isFollowing,
                onFollowClick = { isFollowing = !isFollowing },
                onShareClick = { /* TODO: Xử lý share */ }
            )

            // Tabs: Công thức / Đã thích
            ProfileContentTabs(
                userRecipes = userRecipes,
                likedRecipes = likedRecipes
            )
        }
    }
}


@Composable
private fun ProfileHeader(
    user: User,
    recipeCount: Int,
    followerCount: Int,
    followingCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = user.profileImage),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = WorkSans,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatTextItem(count = recipeCount, label = "Công thức")
            StatTextItem(count = followerCount, label = "Followers")
            StatTextItem(count = followingCount, label = "Following")
        }
    }
}

@Composable
private fun StatTextItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = WorkSans,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = WorkSans,
            color = Color.Gray
        )
    }
}

@Composable
private fun ProfileActions(
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nút Follow/Following
        Button(
            onClick = onFollowClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                // Khi chưa follow (isFollowing = false), dùng màu Cinnabar500
                // Khi đã follow (isFollowing = true), dùng màu Xám nhạt
                containerColor = if (isFollowing) Color.LightGray else Cinnabar500,

                // Màu chữ (text)
                contentColor = if (isFollowing) Color.Black else Color.White
            )
        ) {
            Text(
                text = if (isFollowing) "Following" else "Follow",
                fontSize = 15.sp,
                fontFamily = WorkSans,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Nút Chia sẻ
        OutlinedButton(
            onClick = onShareClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Chia sẻ",
                fontSize = 15.sp,
                fontFamily = WorkSans,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ProfileContentTabs(
    userRecipes: List<Recipe>,
    likedRecipes: List<Recipe>
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Công thức", "Đã thích")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontFamily = WorkSans,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }

        // Nội dung dựa trên tab được chọn
        when (selectedTabIndex) {
            0 -> { // Tab Công thức
                RecipeGrid(recipes = userRecipes)
            }
            1 -> { // Tab Đã thích
                RecipeGrid(recipes = likedRecipes)
            }
        }
    }
}

@Composable
private fun RecipeGrid(recipes: List<Recipe>) {
    if (recipes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Chưa có công thức nào.",
                fontFamily = WorkSans,
                color = Color.Gray
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard()
            }
        }
    }
}
