package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History // Icon đồng hồ cho lịch sử
import androidx.compose.material.icons.filled.Search  // Icon kính lúp cho tìm kiếm
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
            // Hiển thị danh sách (Lịch sử hoặc Gợi ý tùy vào searchText)
            items(suggestions) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 1. Lưu từ khóa vào lịch sử
                            viewModel.saveSearchQuery(item)
                            // 2. Chuyển sang trang kết quả
                            onSuggestionClick(item)
                        }
                        .background(White)
                        .padding(horizontal = 16.dp, vertical = 12.dp), // Tăng padding dọc chút cho dễ bấm
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logic đổi icon:
                    // Nếu đang tìm kiếm (có chữ) -> Hiện kính lúp
                    // Nếu chưa nhập gì (lịch sử) -> Hiện đồng hồ
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
                        color = Black
                    )
                }
            }
        }
    }
}