package com.example.freshcookapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.freshcookapp.ui.component.SuggestKeywordCard
import com.example.freshcookapp.ui.screen.home.SuggestItem
import com.example.freshcookapp.ui.theme.Cinnabar500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlySearchedScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onSearchClick: (String) -> Unit
) {
    val suggestions = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<SuggestItem>>("suggestions") ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ===== Header =====
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Cinnabar500,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBackClick() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Tìm kiếm gần đây",
                style = MaterialTheme.typography.titleLarge,
                color = Cinnabar500
            )
        }

        Spacer(Modifier.height(16.dp))

        // ===== EMPTY STATE =====
        if (suggestions.isEmpty()) {
            // Có thể thêm CategoryEmptyState nếu có, hoặc text đơn giản
            Text(
                text = "Không có tìm kiếm gần đây",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
            )
            return
        }

        // ===== LIST =====
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(suggestions) { item ->
                SuggestKeywordCard(
                    keyword = item.keyword,
                    time = item.timestamp,
                    imageUrl = item.imageUrl,
                    onClick = { onSearchClick(item.keyword) }
                )
            }
        }
    }
}
