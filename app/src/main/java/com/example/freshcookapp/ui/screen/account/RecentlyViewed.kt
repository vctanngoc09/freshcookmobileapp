package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.WorkSans

// Model dữ liệu UI
data class ViewedRecipeModel(
    val id: String,
    val title: String,
    val authorName: String,
    val timeViewed: String,
    val imageUrl: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyViewedScreen(
    onBackClick: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }

    // --- SỬA CÁCH LẤY VIEWMODEL ---
    val viewModel: RecentlyViewedViewModel = viewModel(
        factory = RecentlyViewedViewModel.provideFactory(db)
    )

    val viewedList by viewModel.recentlyViewedList.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = "Xem gần đây",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WorkSans,
                        color = Cinnabar500
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Cinnabar500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (viewedList.isEmpty()) {
            EmptyHistoryState(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
            ) {
                var query by remember { mutableStateOf("") }

                SearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Tìm trong lịch sử...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                val filteredList = remember(viewedList, query) {
                    if (query.isBlank()) viewedList
                    else viewedList.filter { item ->
                        item.title.contains(query, ignoreCase = true) ||
                                item.authorName.contains(query, ignoreCase = true)
                    }
                }

                if (filteredList.isEmpty() && query.isNotBlank()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Không tìm thấy kết quả",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        RecentlyViewedItem(
                            item = item,
                            onRemoveClick = {
                                viewModel.removeFromHistory(item.id)
                            },
                            onClick = {
                                onRecipeClick(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = "Empty History",
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lịch sử trống",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = "Các món ăn bạn vừa xem sẽ xuất hiện tại đây.",
            fontSize = 14.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun RecentlyViewedItem(
    item: ViewedRecipeModel,
    onRemoveClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = item.imageUrl ?: R.drawable.ic_launcher_background
            ),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSans,
                color = Cinnabar500,
                maxLines = 1
            )

            Text(
                text = "Bởi: ${item.authorName}",
                fontSize = 14.sp,
                fontFamily = WorkSans,
                color = Color.Black,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.timeViewed,
                    fontSize = 12.sp,
                    fontFamily = WorkSans,
                    color = Color.Gray
                )
            }
        }

        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Cinnabar500
            )
        }
    }
}