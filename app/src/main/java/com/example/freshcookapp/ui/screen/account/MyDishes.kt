package com.example.freshcookapp.ui.screen.account

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository

// LƯU Ý: RecipeInfo phải được định nghĩa ở ProfileScreen.kt hoặc file Model chung

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDishes(
    onBackClick: () -> Unit,
    onAddNewClick: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    onEditRecipeClick: (String) -> Unit = {}
) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var myRecipes by remember { mutableStateOf<List<RecipeInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }

    // --- LOGIC XÓA MÓN ĂN (CHỈ XÓA CLOUD VÀ STORAGE) ---
    val onDeleteRecipe: (String) -> Unit = deleteLambda@{ recipeId ->
        if (currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()
            return@deleteLambda
        }

        scope.launch {
            try {
                // 1. Xóa document món ăn khỏi Firestore (Cloud)
                firestore.collection("recipes").document(recipeId).delete().await()

                // 2. Xóa ảnh đại diện khỏi Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("recpies_img/$recipeId/main.jpg")

                storageRef.delete().addOnFailureListener {
                    Log.e("MyDishes", "Lỗi xóa ảnh Storage cho $recipeId: ${it.message}")
                }

                Toast.makeText(context, "Đã xóa món ăn thành công!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi xóa món: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    // ---------------------------------------------

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            firestore.collection("recipes")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("MyDishes", "Lỗi lắng nghe dữ liệu: ", e)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        myRecipes = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                
                                // GHI LOG ĐỂ KIỂM TRA GIÁ TRỊ GỐC
                                Log.d("MyDishesDebug", "Document ${doc.id} | timeCook value: ${data["timeCook"]} | type: ${data["timeCook"]?.javaClass?.simpleName}")

                                val timeCookValue = data["timeCook"]
                                val timeCookMinutes = when (timeCookValue) {
                                    is Number -> timeCookValue.toInt() // Xử lý tất cả các kiểu số (Long, Double...)
                                    else -> 0 // Giá trị mặc định nếu null hoặc không phải là số
                                }

                                // Ánh xạ thủ công
                                RecipeInfo(
                                    id = doc.id,
                                    name = data["name"] as? String ?: "",
                                    imageUrl = data["imageUrl"] as? String,
                                    timeCookMinutes = timeCookMinutes,
                                    userId = data["userId"] as? String ?: ""
                                )
                            } catch (ex: Exception) {
                                Log.e("MyDishesDebug", "Lỗi parse document ${doc.id}", ex)
                                null // Bỏ qua document bị lỗi
                            }
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Món của tôi", color = Cinnabar500, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Cinnabar500)
                    }
                },
                actions = {
                    IconButton(onClick = onAddNewClick) {
                        Icon(Icons.Default.Add, "Add New Dish", tint = Cinnabar500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Cinnabar500)
            } else if (myRecipes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Bạn chưa tạo món ăn nào.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddNewClick, colors = ButtonDefaults.buttonColors(containerColor = Cinnabar500)) {
                        Text("Tạo món ngay")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myRecipes) { recipe ->
                        MyDishItem(
                            recipe = recipe,
                            onClick = {
                                if (recipe.id.isNotEmpty()) {
                                    onRecipeClick(recipe.id)
                                }
                            },
                            onDeleteClick = { onDeleteRecipe(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyDishItem(
    recipe: RecipeInfo,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = recipe.imageUrl ?: R.drawable.img_food1),
                contentDescription = recipe.name,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                // Dữ liệu thời gian sẽ đúng sau khi sửa RecipeInfo
                Text(
                    text = "${recipe.timeCookMinutes} phút",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // NÚT THÙNG RÁC TRỰC TIẾP
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Dish",
                    tint = Cinnabar500
                )
            }
        }
    }
}