package com.example.freshcookapp.ui.screen.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Filter(
    onBackClick: () -> Unit = {},
    onApply: () -> Unit = {}
) {
    var includedIngredients by remember { mutableStateOf(listOf("hành", "ớt", "xả")) }
    var excludedIngredients by remember { mutableStateOf(listOf("ớt")) }
    var difficulty by remember { mutableStateOf("Dễ") }
    var timeCook by remember { mutableStateOf(5f) }
    var test by remember { mutableStateOf("") }
    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Cinnabar500,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Sàng lọc",
                    style = MaterialTheme.typography.titleLarge,
                    color = Cinnabar500
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                /** ====== NGUYÊN LIỆU BAO GỒM ====== */
                item {
                    Text(
                        text = "Hiển thị các món với:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    CustomTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = "Gõ vào tên các nguyên liệu..."
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        includedIngredients.forEach { item ->
                            FilterChip(text = item) {
                                includedIngredients = includedIngredients - item
                            }
                        }
                    }
                }

                /** ====== NGUYÊN LIỆU DỊ ỨNG ====== */
                item {
                    Text(
                        text = "Hiển thị các nguyên liệu dị ứng:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    CustomTextField(
                        value = test,
                        onValueChange = {test = it},
                        placeholder = "Gõ vào tên các nguyên liệu..."
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        excludedIngredients.forEach { item ->
                            FilterChip(text = item) {
                                excludedIngredients = excludedIngredients - item
                            }
                        }
                    }
                }

                /** ====== ĐỘ KHÓ ====== */
                item {
                    Text(
                        text = "Độ khó",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Dễ", "Trung", "Khó").forEach { level ->
                            DifficultyChip(
                                text = level,
                                isSelected = difficulty == level,
                                onClick = { difficulty = level }
                            )
                        }
                    }
                }

                /** ====== THỜI GIAN NẤU ====== */
                item {
                    Text(
                        text = "Thời gian nấu",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${timeCook.toInt()} phút", color = Cinnabar500)
                        Text("5–60 phút", color = Cinnabar400)
                    }

                    Slider(
                        value = timeCook,
                        onValueChange = { timeCook = it },
                        valueRange = 5f..60f,
                        colors = SliderDefaults.colors(
                            thumbColor = Cinnabar500,
                            activeTrackColor = Cinnabar500
                        )
                    )
                }

                /** ====== NÚT HÀNH ĐỘNG ====== */
                item {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                includedIngredients = emptyList()
                                excludedIngredients = emptyList()
                                difficulty = "Dễ"
                                timeCook = 5f
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Cinnabar50),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Xóa", color = Cinnabar500)
                        }

                        Button(
                            onClick = onApply,
                            colors = ButtonDefaults.buttonColors(containerColor = Cinnabar800),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hiển thị món", color = White)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }

    }
}

@Composable
fun FilterChip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Cinnabar400, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = Cinnabar700, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = Cinnabar400,
            modifier = Modifier
                .size(16.dp)
                .clickable { onRemove() }
        )
    }
}

@Composable
fun DifficultyChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) Cinnabar500 else Color.Gray,
                shape = RoundedCornerShape(25.dp)
            )
            .background(if (isSelected) Cinnabar100 else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Cinnabar500 else Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}