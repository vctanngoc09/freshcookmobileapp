package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.ui.component.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    keyword: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SearchResultViewModel = viewModel(
        factory = SearchResultViewModelFactory(keyword, context.applicationContext as Application)
    )

    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết quả cho \"$keyword\"") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(results) { recipe ->
                RecipeCard(
                    imageUrl = recipe.imageUrl,
                    title = recipe.name,
                    time = "${recipe.timeCookMinutes ?: 0} phút",
                    level = "Dễ", // bạn tự map nếu có level
                    isFavorite = false,
                    onFavoriteClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}