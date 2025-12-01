package com.example.freshcookapp.ui.screen.search

import android.app.Application
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.RecipeCard
import com.example.freshcookapp.ui.theme.Cinnabar500

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
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
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
