package com.example.freshcookapp.ui.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.* // Thêm import Material3
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.R
// Xóa ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Black
import com.example.freshcookapp.ui.theme.Cinnabar400
import com.example.freshcookapp.ui.theme.Cinnabar500

@OptIn(ExperimentalMaterial3Api::class) // Thêm OptIn
@Composable
fun Search(onBackClick: () -> Unit, onFilterClick: () -> Unit) {
    var searchText by remember { mutableStateOf("") }

    // Dữ liệu giả lập (lịch sử tìm kiếm)
    val recentSearches = remember {
        mutableStateListOf("trứng", "trứng chiên", "cá kho")
    }

    // 1. Dùng Scaffold thay vì ScreenContainer
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            // 2. Tạo TopAppBar để chứa nút back và SearchBar
            TopAppBar(
                title = {
                    // Ô tìm kiếm tái sử dụng
                    SearchBar(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = "Tìm kiếm",
                        onFilterClick = onFilterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp) // Thêm padding cuối
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding -> // 3. Lấy innerPadding
        // 4. Áp dụng innerPadding cho nội dung
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Áp dụng padding ở đây
                .padding(top = 16.dp), // Thêm 1 chút đệm ở trên
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(recentSearches, key = { it }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Thêm padding ngang
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "History",
                            tint = Cinnabar400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item,
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = { recentSearches.remove(item) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Cinnabar400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}