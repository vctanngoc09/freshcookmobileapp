package com.example.freshcookapp.ui.screen.account

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.ui.component.PrimaryButton
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.GrayLight
import com.example.freshcookapp.ui.theme.WorkSans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Giới tính") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isNewUser by remember { mutableStateOf(true) }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // Fetch user data from Firestore
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            email = currentUser.email ?: ""
            val defaultUsername = currentUser.email?.split('@')?.get(0) ?: ""

            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        isNewUser = false
                        fullName = document.getString("fullName") ?: currentUser.displayName ?: ""
                        username = document.getString("username")?.takeIf { it.isNotBlank() } ?: defaultUsername
                        dateOfBirth = document.getString("dateOfBirth") ?: ""
                        gender = document.getString("gender") ?: "Giới tính"
                        photoUrl = document.getString("photoUrl")
                    } else {
                        isNewUser = true
                        fullName = currentUser.displayName ?: ""
                        username = defaultUsername
                        photoUrl = currentUser.photoUrl?.toString()
                    }
                }
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
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

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                            .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = selectedImageUri ?: photoUrl ?: com.example.freshcookapp.R.drawable.avatar1
                            ),
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = "Chỉnh sửa ảnh",
                        fontSize = 12.sp,
                        fontFamily = WorkSans,
                        color = Cinnabar500,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 20.dp)
                            .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Full Name
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Họ tên", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = GrayLight, focusedContainerColor = GrayLight, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Cinnabar500)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Username
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Tên người dùng", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = GrayLight, focusedContainerColor = GrayLight, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Cinnabar500)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date of Birth
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ngày sinh", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))

                        // --- SỬA LỖI CLICK TRÊN MÁY THẬT ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showDatePicker = true } // Click hoạt động vì enabled=false bên dưới
                        ) {
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { },
                                modifier = Modifier.fillMaxSize(),
                                shape = MaterialTheme.shapes.medium,
                                enabled = false, // QUAN TRỌNG: Tắt hẳn tương tác để sự kiện click lọt ra ngoài Box
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = GrayLight, // Màu nền khi disable
                                    disabledTextColor = Color.Black,    // Màu chữ vẫn đen đậm
                                    disabledBorderColor = Color.Transparent // Border trong suốt
                                )
                            )
                        }
                    }

                    // Gender
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Giới tính", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                        Box {
                            // --- SỬA LỖI CLICK TRÊN MÁY THẬT ---
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { expanded = true }
                            ) {
                                OutlinedTextField(
                                    value = gender,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxSize(),
                                    enabled = false, // QUAN TRỌNG: Tắt tương tác
                                    shape = MaterialTheme.shapes.medium,
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.Black) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor = GrayLight,
                                        disabledTextColor = Color.Black,
                                        disabledBorderColor = Color.Transparent,
                                        disabledTrailingIconColor = Color.Black // Icon vẫn đen
                                    )
                                )
                            }

                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(text = { Text("Nam") }, onClick = { gender = "Nam"; expanded = false })
                                DropdownMenuItem(text = { Text("Nữ") }, onClick = { gender = "Nữ"; expanded = false })
                                DropdownMenuItem(text = { Text("Khác") }, onClick = { gender = "Khác"; expanded = false })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                PrimaryButton(
                    text = "Lưu",
                    onClick = {
                        if (currentUser != null) {
                            isLoading = true

                            val userProfile = mutableMapOf<String, Any?>(
                                "uid" to currentUser.uid,
                                "fullName" to fullName.takeIf { it.isNotBlank() },
                                "username" to username.takeIf { it.isNotBlank() },
                                "dateOfBirth" to dateOfBirth.takeIf { it.isNotBlank() },
                                "gender" to gender.takeIf { it != "Giới tính" },
                                "email" to email,
                                "photoUrl" to (selectedImageUri?.toString() ?: photoUrl)
                            )

                            if (isNewUser) {
                                userProfile["followerCount"] = 0
                                userProfile["followingCount"] = 0
                                userProfile["dishCount"] = 0
                            }

                            firestore.collection("users").document(currentUser.uid)
                                .set(userProfile, SetOptions.merge())
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "Đã lưu hồ sơ!", Toast.LENGTH_SHORT).show()
                                    onSaveClick()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            dateOfBirth = formatter.format(Date(it))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}