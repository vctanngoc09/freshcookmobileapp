package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDishes(
    onBackClick: () -> Unit,
    onAddNewClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    // --- STATE ---
    var myRecipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // --- LOGIC LẤY DỮ LIỆU TỪ FIREBASE ---
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("recipes")
                .whereEqualTo("userId", currentUser.uid)
                // Sắp xếp theo ngày tạo mới nhất (Cần tạo Index trong Firebase nếu báo lỗi đỏ trong Logcat)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                // Map dữ liệu thủ công để an toàn
                                Recipe(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "Không tên",
                                    imageUrl = doc.getString("imageUrl"),
                                    timeCook = doc.getLong("timeCook")?.toInt() ?: 0,
                                    difficulty = doc.getString("difficulty") ?: "Dễ",
                                    // Các trường phụ
                                    author = Author(id = currentUser.uid, name = "", avatarUrl = null),
                                    description = "",
                                    ingredients = emptyList(),
                                    instructions = emptyList(),
                                    relatedRecipes = emptyList(),
                                    isFavorite = false
                                )
                            } catch (ex: Exception) {
                                null
                            }
                        }
                        myRecipes = list
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // --- GIAO DIỆN (UI) ---
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Món của tôi (${myRecipes.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WorkSans,
                        color = Cinnabar500
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Cinnabar500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewClick,
                containerColor = Cinnabar500,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Thêm món mới", fontFamily = WorkSans, fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9)) // Màu nền nhẹ cho danh sách
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Cinnabar500
                )
            } else if (myRecipes.isEmpty()) {
                // Hiển thị khi không có món ăn nào
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bạn chưa đăng công thức nào.",
                        color = Color.Gray,
                        fontFamily = WorkSans,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hãy chia sẻ món ngon đầu tiên nhé!",
                        color = Color.LightGray,
                        fontFamily = WorkSans,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Danh sách món ăn
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myRecipes) { recipe ->
                        // Tái sử dụng Component RecipeCard chuẩn
                        RecipeCard(
                            imageUrl = recipe.imageUrl,
                            name = recipe.name,
                            timeCook = recipe.timeCook,
                            difficulty = recipe.difficulty ?: "Dễ",
                            isFavorite = false, // Trong trang quản lý của mình thì không cần nút tim
                            onFavoriteClick = {},
                            modifier = Modifier.clickable { onRecipeClick(recipe.id) }
                        )
                    }
                    // Khoảng trống dưới cùng để không bị nút FAB che khuất
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}