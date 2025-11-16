package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNotificationClick: () -> Unit = {},
    onMyDishesClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onFollowerClick: (userId: String) -> Unit = {},
    onFollowingClick: (userId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf("Tên người dùng") }
    var username by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var followerCount by remember { mutableStateOf(0L) }
    var followingCount by remember { mutableStateOf(0L) }
    var dishCount by remember { mutableStateOf(0L) }
    var recipes by remember { mutableStateOf<List<RecipeInfo>>(emptyList()) }

    // Lắng nghe dữ liệu người dùng và các món ăn
    DisposableEffect(currentUser) {
        val userId = currentUser?.uid ?: return@DisposableEffect onDispose {}

        val userListener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    fullName = snapshot.getString("fullName") ?: currentUser.displayName ?: "Tên người dùng"
                    username = snapshot.getString("username") ?: currentUser.email?.split('@')?.get(0) ?: ""
                    photoUrl = snapshot.getString("photoUrl") ?: currentUser.photoUrl?.toString()
                    followerCount = snapshot.getLong("followerCount") ?: 0L
                    followingCount = snapshot.getLong("followingCount") ?: 0L
                    dishCount = snapshot.getLong("dishCount") ?: 0L
                }
            }

        val recipesListener = firestore.collection("recipes")
            .whereEqualTo("userId", userId)
            .limit(4) // Chỉ tải 4 món ăn đầu tiên để hiển thị
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    recipes = snapshot.documents.map { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: ""
                        val imageUrl = doc.getString("imageUrl")
                        val timeCook = doc.getLong("timeCookMinutes")?.toInt() ?: 0
                        RecipeInfo(id = id, name = name, imageUrl = imageUrl, timeCookMinutes = timeCook)
                    }
                }
            }

        onDispose {
            userListener.remove()
            recipesListener.remove()
        }
    }

    ScreenContainer {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = Cinnabar500)
                }
                Text("Tài khoản", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Cinnabar500)
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = Cinnabar500)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = photoUrl ?: R.drawable.avatar1),
                            contentDescription = "Profile",
                            modifier = Modifier.size(140.dp).clip(CircleShape).clickable { onEditProfileClick() },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (username.isNotBlank()) {
                            Text("@$username", fontSize = 14.sp, fontFamily = WorkSans, color = Color.Gray)
                        }
                    }
                }

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
                        StatItem(count = followerCount.toString(), label = "Follower", onClick = { currentUser?.uid?.let { onFollowerClick(it) } })
                        Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                        StatItem(count = dishCount.toString(), label = "Món", onClick = onMyDishesClick)
                        Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.White.copy(alpha = 0.5f))
                        StatItem(count = followingCount.toString(), label = "Following", onClick = { currentUser?.uid?.let { onFollowingClick(it) } })
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRecentlyViewedClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Cinnabar500),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Cinnabar500),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Xem gần đây", fontSize = 15.sp, fontFamily = WorkSans, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onMyDishesClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cinnabar500),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Món của tôi", fontSize = 15.sp, fontFamily = WorkSans, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Chỉ hiển thị phần này nếu người dùng có món ăn
                if (recipes.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "Xem tất cả",
                                fontSize = 14.sp,
                                fontFamily = WorkSans,
                                color = Cinnabar500,
                                fontWeight = FontWeight.Medium,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.Underline),
                                modifier = Modifier.clickable { onMyDishesClick() }
                            )
                        }
                    }

                    item {
                        // Tính toán chiều cao cho lưới dựa trên số lượng item
                        val gridRows = (recipes.size + 1) / 2
                        val gridHeight = (gridRows * 200 + (gridRows - 1).coerceAtLeast(0) * 12).dp

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gridHeight) //Sử dụng chiều cao cố định, đã tính toán
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 12.dp),
                            userScrollEnabled = false // Vô hiệu hóa cuộn cho lưới bên trong
                        ) {
                            items(recipes) { recipe ->
                                RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tạo món ăn đầu tiên của bạn!", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 14.sp, fontFamily = WorkSans, color = Color.White)
    }
}

@Composable
fun RecipeCard(recipe: RecipeInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Image(painter = rememberAsyncImagePainter(model = recipe.imageUrl ?: R.drawable.img_food1), "Recipe Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Icon(Icons.Default.Favorite, "Favorite", tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(24.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape).padding(4.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(recipe.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = WorkSans, color = Color.White, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, "Time", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${recipe.timeCookMinutes} phút", fontSize = 12.sp, fontFamily = WorkSans, color = Color.White)
                    }
                }
            }
        }
    }
}
