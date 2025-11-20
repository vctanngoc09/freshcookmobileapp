package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    // Callback này sẽ trả về TÊN món ăn (String) khi bấm vào
    onSuggestionClick: (String) -> Unit
) {
    val context = LocalContext.current

    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(context.applicationContext as Application)
    )

    val searchText by viewModel.query.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        value = searchText,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = "Tìm kiếm",
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
                            null,
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
            // Hiển thị danh sách gợi ý (Text đơn giản)
            items(suggestions) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(item) }
                        .background(White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Cinnabar400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item, // Hiển thị tên món
                        fontSize = 15.sp,
                        color = Black
                    )
                }
            }
        }
    }
}