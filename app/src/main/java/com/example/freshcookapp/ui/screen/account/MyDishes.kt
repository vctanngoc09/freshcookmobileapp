package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

// Giữ lại data class RecipeInfo nếu cần dùng chung
data class RecipeInfo(
    val id: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val timeCookMinutes: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDishes(
    onBackClick: () -> Unit,
    onAddNewClick: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {}
) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var myRecipes by remember { mutableStateOf<List<RecipeInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            firestore.collection("recipes")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        myRecipes = snapshot.documents.mapNotNull {
                            it.toObject<RecipeInfo>()?.copy(id = it.id)
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
            myRecipes = emptyList()
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (myRecipes.isEmpty()) {
                Text("Bạn chưa tạo món ăn nào.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myRecipes) { recipe ->
                        MyDishItem(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun MyDishItem(recipe: RecipeInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = recipe.imageUrl ?: R.drawable.img_food1),
                contentDescription = recipe.name,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("${recipe.timeCookMinutes} phút", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
