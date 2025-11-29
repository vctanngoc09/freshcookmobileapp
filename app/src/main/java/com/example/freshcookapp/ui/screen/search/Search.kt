package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Black
import com.example.freshcookapp.ui.theme.Cinnabar400
import com.example.freshcookapp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    keyword: String? = null,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    val context = LocalContext.current

    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(context.applicationContext as Application)
    )

    val searchText by viewModel.query.collectAsState()
    LaunchedEffect(keyword) {
        if (keyword != null) {
            viewModel.onQueryChange(keyword)
        }
    }
    val suggestions by viewModel.suggestions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        value = searchText,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = "Tìm kiếm món ăn...",
                        onFilterClick = onFilterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(padding)
                .padding(top = 16.dp)
        ) {
            if (searchText.isBlank() && suggestions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lịch sử tìm kiếm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.clearAllHistory() }) {
                            Text("Xóa tất cả", color = Cinnabar400)
                        }
                    }
                }
            }

            items(suggestions) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.saveSearchQuery(item)
                            onSuggestionClick(item)
                        }
                        .background(White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = if (searchText.isNotBlank()) Icons.Default.Search else Icons.Default.History
                    val iconColor = if (searchText.isNotBlank()) Cinnabar400 else Color.Gray

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = item,
                        fontSize = 16.sp,
                        color = Black,
                        modifier = Modifier.weight(1f)
                    )

                    if (searchText.isBlank()) {
                        IconButton(onClick = { viewModel.deleteSearchQuery(item) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
