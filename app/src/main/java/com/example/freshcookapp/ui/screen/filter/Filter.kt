package com.example.freshcookapp.ui.screen.filter

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.screen.newcook.DurationPickerDialog
import com.example.freshcookapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Filter(
    onBackClick: () -> Unit = {},
    onApply: (List<String>, List<String>, String, Float) -> Unit = { _, _, _, _ -> }
) {
    var includedIngredients by remember { mutableStateOf(emptyList<String>()) }
    var excludedIngredients by remember { mutableStateOf(emptyList<String>()) }
    var difficulty by remember { mutableStateOf("Dễ") }
    var timeCook by remember { mutableStateOf(5f) }
    var includedInput by remember { mutableStateOf("") }
    var excludedInput by remember { mutableStateOf("") }

    ScreenContainer{
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Sàng lọc",
                            style = MaterialTheme.typography.titleLarge,
                            color = Cinnabar500
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
                                tint = Cinnabar500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
                )
            }
        ) { innerPadding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /** ==== NGUYÊN LIỆU BAO GỒM ==== */
                item {
                    Text(
                        "Hiển thị các món với:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CustomTextField(
                            value = includedInput,
                            onValueChange = { includedInput = it },
                            placeholder = "Gõ vào tên các nguyên liệu...",
                            modifier = Modifier.weight(1f)
                        )
                        if (includedInput.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    includedIngredients = includedIngredients + includedInput.trim()
                                    includedInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cinnabar500),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Thêm", color = Color.White)
                            }
                        }
                    }
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

                /** ==== NGUYÊN LIỆU DỊ ỨNG ==== */
                item {
                    Text(
                        "Loại trừ nguyên liệu:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CustomTextField(
                            value = excludedInput,
                            onValueChange = { excludedInput = it },
                            placeholder = "Gõ vào tên các nguyên liệu...",
                            modifier = Modifier.weight(1f)
                        )
                        if (excludedInput.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    excludedIngredients = excludedIngredients + excludedInput.trim()
                                    excludedInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cinnabar500),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Thêm", color = Color.White)
                            }
                        }
                    }
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

                /** ==== ĐỘ KHÓ ==== */
                item {
                    Text(
                        "Độ khó",
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

                /** ==== THỜI GIAN ==== */
                item {
                    var showTimeDialog by remember { mutableStateOf(false) }

                    Text(
                        "Thời gian nấu",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Cinnabar400, RoundedCornerShape(10.dp))
                            .clickable { showTimeDialog = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (timeCook.toInt() > 0) "${timeCook.toInt()} phút" else "Chọn thời gian",
                            color = if (timeCook > 0) Cinnabar500 else Color.Gray
                        )
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Cinnabar500)
                    }

                    if (showTimeDialog) {
                        DurationPickerDialog(
                            initialMinutes = timeCook.toInt(),
                            onDismiss = { showTimeDialog = false },
                            onConfirm = { minutes ->
                                timeCook = minutes.toFloat()
                                showTimeDialog = false
                            }
                        )
                    }
                }

                /** ==== NÚT HÀNH ĐỘNG ==== */
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                includedIngredients = emptyList()
                                excludedIngredients = emptyList()
                                includedInput = ""
                                excludedInput = ""
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
                            onClick = { onApply(includedIngredients, excludedIngredients, difficulty, timeCook) },
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
        Text(text, color = Cinnabar700)
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
fun DifficultyChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) Cinnabar500 else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp), // ⭐ gọn hơn
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,          // ⭐ nhỏ lại
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Cinnabar500 else Color(0xFF6D6D6D) // mềm hơn
            )
        )
    }
}