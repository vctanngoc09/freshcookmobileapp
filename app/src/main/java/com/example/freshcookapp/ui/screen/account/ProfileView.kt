package com.example.freshcookapp.ui.screen.account

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    // --- THÊM THAM SỐ NÀY ĐỂ BẤM VÀO MÓN ---
    onRecipeClick: (String) -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Dữ liệu hiển thị
    var fullName by remember { mutableStateOf("Đang tải...") } // Mặc định hiển thị đang tải
    var username by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    var followerCount by remember { mutableIntStateOf(0) }
    var followingCount by remember { mutableIntStateOf(0) }
    var recipeCount by remember { mutableIntStateOf(0) }

    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var authorRecipes by remember { mutableStateOf<List<RecipeInfo>>(emptyList()) }

    // 1. LẤY THÔNG TIN USER (Tên, Ảnh...)
    LaunchedEffect(userId) {
        firestore.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                // Ưu tiên hiển thị FullName, nếu không có thì lấy Username
                fullName = snapshot.getString("fullName") ?: "Người dùng"
                username = snapshot.getString("username") ?: ""
                photoUrl = snapshot.getString("photoUrl")
                isLoading = false
            }
        }
    }

    // 2. TỰ ĐẾM SỐ LIỆU & LẤY DANH SÁCH MÓN
    LaunchedEffect(userId) {
        // Đếm Follower
        firestore.collection("users").document(userId).collection("followers")
            .addSnapshotListener { s, _ -> if (s != null) followerCount = s.size() }

        // Đếm Following
        firestore.collection("users").document(userId).collection("following")
            .addSnapshotListener { s, _ -> if (s != null) followingCount = s.size() }

        // Lấy danh sách Món ăn
        firestore.collection("recipes").whereEqualTo("userId", userId)
            .addSnapshotListener { s, _ ->
                if (s != null) {
                    recipeCount = s.size()
                    authorRecipes = s.documents.mapNotNull { doc ->
                        doc.toObject<RecipeInfo>()?.copy(id = doc.id)
                    }
                }
            }
    }

    // 3. CHECK FOLLOW
    DisposableEffect(currentUserId, userId) {
        val listener = if (currentUserId != null) {
            firestore.collection("users").document(currentUserId)
                .collection("following").document(userId)
                .addSnapshotListener { document, _ ->
                    isFollowing = document != null && document.exists()
                }
        } else { null }
        onDispose { listener?.remove() }
    }

    // 4. LOGIC FOLLOW
    val onFollowClick: () -> Unit = {
        if (currentUserId == null) {
            Toast.makeText(context, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show()
        } else if (currentUserId == userId) {
            // Không follow chính mình
        } else {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val viewedUserRef = firestore.collection("users").document(userId)
            val followingRef = currentUserRef.collection("following").document(userId)
            val followerRef = viewedUserRef.collection("followers").document(currentUserId)

            firestore.runTransaction { transaction ->
                val isCurrentlyFollowing = transaction.get(followingRef).exists()
                if (isCurrentlyFollowing) {
                    transaction.delete(followingRef)
                    transaction.delete(followerRef)
                } else {
                    transaction.set(followingRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                    transaction.set(followerRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // --- HIỂN THỊ TÊN NGƯỜI DÙNG Ở ĐÂY ---
                title = {
                    Text(
                        text = fullName, // Hiển thị Họ tên đầy đủ
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Cinnabar500)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- INFO ---
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Image(
                        painter = rememberAsyncImagePainter(model = photoUrl ?: R.drawable.avatar1),
                        contentDescription = "Profile Image",
                        modifier = Modifier.size(120.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    if (username.isNotBlank()) {
                        Text("@$username", fontSize = 16.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- STATS ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AuthorStatItem(count = followerCount.toString(), label = "Followers")
                        AuthorStatItem(count = recipeCount.toString(), label = "Món ăn")
                        AuthorStatItem(count = followingCount.toString(), label = "Following")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- BUTTON ---
                if (currentUserId != userId) {
                    item {
                        Button(
                            onClick = onFollowClick,
                            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color.LightGray else Cinnabar500,
                                contentColor = if (isFollowing) Color.Black else Color.White
                            )
                        ) {
                            Text(if (isFollowing) "Đang theo dõi" else "Theo dõi")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(thickness = 8.dp, color = Color(0xFFF5F5F5))
                    }
                }

                // --- LIST MÓN ĂN ---
                if (authorRecipes.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Người dùng này chưa đăng món nào.", color = Color.Gray)
                        }
                    }
                } else {
                    items(authorRecipes) { recipe ->
                        AuthorDishItem(
                            recipe = recipe,
                            // --- KẾT NỐI SỰ KIỆN CLICK ---
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}

// Component món ăn: Thêm sự kiện onClick
@Composable
fun AuthorDishItem(recipe: RecipeInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(100.dp)
            .clickable(onClick = onClick), // --- CLICKABLE Ở ĐÂY ---
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(model = recipe.imageUrl ?: R.drawable.img_food1),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${recipe.timeCookMinutes} phút", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun AuthorStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
    }
}