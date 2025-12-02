package com.example.freshcookapp.ui.screen.account

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    // State cho việc xóa món
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDeleteId by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // --- LOGIC LẤY DỮ LIỆU TỪ FIREBASE ---
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            firestore.collection("recipes")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                Recipe(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "Không tên",
                                    imageUrl = doc.getString("imageUrl"),
                                    timeCook = doc.getLong("timeCook")?.toInt() ?: 0,
                                    difficulty = doc.getString("difficulty") ?: "Dễ",
                                    author = Author(id = currentUser.uid, name = "", avatarUrl = null),
                                    description = "",
                                    ingredients = emptyList(),
                                    instructions = emptyList(),
                                    relatedRecipes = emptyList(),
                                    isFavorite = false
                                )
                            } catch (ex: Exception) { null }
                        }
                        myRecipes = list
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // Hàm xóa món ăn
    fun deleteRecipe(recipeId: String) {
        firestore.collection("recipes").document(recipeId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Đã xóa món ăn", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi khi xóa: ${it.message}", Toast.LENGTH_SHORT).show()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cinnabar500)
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
                .background(Color(0xFFF9F9F9))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Cinnabar500)
            } else if (myRecipes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Bạn chưa đăng công thức nào.", color = Color.Gray, fontFamily = WorkSans)
                    Text("Hãy chia sẻ món ngon đầu tiên nhé!", color = Color.LightGray, fontFamily = WorkSans, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myRecipes) { recipe ->
                        // --- ITEM MÓN ĂN CÓ NÚT 3 CHẤM ---
                        RecipeItemWithMenu(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) },
                            onDeleteClick = {
                                recipeToDeleteId = recipe.id
                                showDeleteDialog = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Dialog xác nhận xóa
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xóa món ăn?") },
                text = { Text("Bạn có chắc chắn muốn xóa món này không? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            recipeToDeleteId?.let { deleteRecipe(it) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

// Component riêng để quản lý Menu cho từng Item
@Composable
fun RecipeItemWithMenu(
    recipe: Recipe,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // 1. Recipe Card (Full width)
        RecipeCard(
            imageUrl = recipe.imageUrl,
            name = recipe.name,
            timeCook = recipe.timeCook,
            difficulty = recipe.difficulty ?: "Dễ",
            isFavorite = false,
            onFavoriteClick = {},
            modifier = Modifier
                .fillMaxWidth() // 🔥 Fix lỗi: Hiển thị full hàng
                .clickable { onClick() }
        )

        // 2. Nút 3 chấm (Góc trên phải)
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f), CircleShape) // Nền trắng mờ để dễ nhìn trên ảnh
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Menu Dropdown
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                DropdownMenuItem(
                    text = { Text("Xóa món này", color = Color.Red) },
                    onClick = {
                        expanded = false
                        onDeleteClick()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                )
            }
        }
    }
}