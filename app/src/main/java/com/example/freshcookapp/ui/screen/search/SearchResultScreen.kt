package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    keyword: String? = null,
    includedIngredients: List<String> = emptyList(),
    excludedIngredients: List<String> = emptyList(),
    difficulty: String = "",
    timeCook: Float = 0f,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val viewModel: SearchResultViewModel = viewModel(
        factory = SearchResultViewModelFactory(
            LocalContext.current.applicationContext as Application,
            keyword,
            includedIngredients,
            excludedIngredients,
            difficulty,
            timeCook
        )
    )

    val results by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết quả", color = Cinnabar500) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Cinnabar500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Cinnabar500)
            }
        } else {
            if (results.isEmpty()) {

                // ⭐ HIỂN THỊ KHI KHÔNG CÓ KẾT QUẢ
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyFilterState()
                }

            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    // ⭐ DÒNG HIỂN THỊ TỔNG SỐ KẾT QUẢ
                    Text(
                        text = "Tìm thấy ${results.size} món phù hợp",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Cinnabar500,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(results) { recipe ->
                            RecipeCard(
                                imageUrl = recipe.imageUrl,
                                name = recipe.name,
                                timeCook = recipe.timeCook,
                                difficulty = recipe.difficulty,
                                isFavorite = false,
                                onFavoriteClick = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRecipeClick(recipe.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun EmptyFilterState(modifier: Modifier = Modifier) {

    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_no_result),
            contentDescription = "Empty",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Không có món nào phù hợp",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            fontFamily = WorkSans
        )

        Text(
            text = "Hãy thử thay đổi lại nguyên liệu hoặc thời gian nấu.",
            fontSize = 14.sp,
            color = muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
            fontFamily = WorkSans
        )
    }
}

