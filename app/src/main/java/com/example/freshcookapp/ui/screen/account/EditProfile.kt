package com.example.freshcookapp.ui.screen.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshcookapp.R
import com.example.freshcookapp.ui.component.CustomTextField
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.GrayLight
import com.example.freshcookapp.ui.theme.WorkSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("Võ Cao Tấn Ngọc") }
    var username by remember { mutableStateOf("ngocvctn09") }
    var dateOfBirth by remember { mutableStateOf("18/07/2005") }
    var gender by remember { mutableStateOf("Giới tính") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Cinnabar500
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Chỉnh sửa trang cá nhân",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSans,
                color = Cinnabar500
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile Image
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    // Profile image
                    Image(
                        painter = painterResource(id = R.drawable.avatar1),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Edit photo text
                Text(
                    text = "Chỉnh sửa ảnh",
                    fontSize = 12.sp,
                    fontFamily = WorkSans,
                    color = Cinnabar500,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 20.dp)
                        .clickable { }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Full Name Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Họ tên",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = WorkSans,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GrayLight,
                        focusedContainerColor = GrayLight,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Cinnabar500
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tên người dùng",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = WorkSans,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GrayLight,
                        focusedContainerColor = GrayLight,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Cinnabar500
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth and Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date of Birth
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ngày sinh",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = WorkSans,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = GrayLight,
                            focusedContainerColor = GrayLight,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Cinnabar500
                        )
                    )
                }

                // Gender Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Giới tính",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = WorkSans,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { expanded = true },
                            readOnly = true,
                            shape = MaterialTheme.shapes.medium,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = GrayLight,
                                focusedContainerColor = GrayLight,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Cinnabar500
                            )
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nam") },
                                onClick = {
                                    gender = "Nam"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nữ") },
                                onClick = {
                                    gender = "Nữ"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Khác") },
                                onClick = {
                                    gender = "Khác"
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Save Button
            PrimaryButton(
                text = "Save",
                onClick = onSaveClick
            )
        }
    }
}
