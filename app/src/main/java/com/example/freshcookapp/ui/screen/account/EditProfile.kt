package com.example.freshcookapp.ui.screen.account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

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

    val isFormValid by remember {
        derivedStateOf { fullName.isNotBlank() && username.isNotBlank() && !username.contains(" ") }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )


    LaunchedEffect(auth.currentUser) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            email = currentUser.email ?: ""
            val defaultUsername = currentUser.email?.split('@')?.get(0) ?: ""
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        fullName = document.getString("fullName") ?: currentUser.displayName ?: ""
                        username = document.getString("username")?.takeIf { it.isNotBlank() } ?: defaultUsername
                        dateOfBirth = document.getString("dateOfBirth") ?: ""
                        gender = document.getString("gender") ?: "Giới tính"
                        photoUrl = document.getString("photoUrl")
                    } else {
                        fullName = currentUser.displayName ?: ""
                        username = defaultUsername
                        photoUrl = currentUser.photoUrl?.toString()
                        dateOfBirth = ""
                        gender = "Giới tính"
                    }
                }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Cinnabar500) }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chỉnh sửa trang cá nhân", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans, color = Cinnabar500)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Image(painter = rememberAsyncImagePainter(model = selectedImageUri ?: photoUrl ?: com.example.freshcookapp.R.drawable.avatar1), contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Text(text = "Chỉnh sửa ảnh", fontSize = 12.sp, fontFamily = WorkSans, color = Cinnabar500, modifier = Modifier.align(Alignment.BottomCenter).offset(y = 20.dp).clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
                }
                Spacer(modifier = Modifier.height(40.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Họ tên *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        isError = fullName.isBlank(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Cinnabar500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Cinnabar500
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Tên người dùng *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.filter { char -> !char.isWhitespace() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        isError = username.isBlank() || username.contains(" "),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Cinnabar500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Cinnabar500
                        )
                    )

                    if (username.contains(" ")) {
                        Text(
                            "Tên người dùng không được chứa dấu cách.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Ngày sinh",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { },
                                modifier = Modifier.fillMaxSize(),
                                shape = MaterialTheme.shapes.medium,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Giới tính",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box {
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
                                    enabled = false,
                                    shape = MaterialTheme.shapes.medium,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
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
                    enabled = isFormValid,
                    onClick = {
                        val currentUser = auth.currentUser
                        if (currentUser == null) return@PrimaryButton
                        isLoading = true
                        
                        val scope = CoroutineScope(Dispatchers.IO)
                        scope.launch {
                            try {
                                val finalPhotoUrl = if (selectedImageUri != null) {
                                    val compressedImageData = context.compressImage(selectedImageUri!!)
                                    val photoRef = storage.reference.child("profile_images/${currentUser.uid}")
                                    val uploadTask = photoRef.putBytes(compressedImageData).await()
                                    uploadTask.storage.downloadUrl.await().toString()
                                } else {
                                    photoUrl
                                }

                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName.trim())
                                    .build()
                                currentUser.updateProfile(profileUpdates).await()

                                val userProfileData = mutableMapOf<String, Any>()
                                userProfileData["fullName"] = fullName.trim()
                                userProfileData["username"] = username.trim()
                                if (dateOfBirth.isNotBlank()) userProfileData["dateOfBirth"] = dateOfBirth
                                if (gender != "Giới tính") userProfileData["gender"] = gender
                                if (finalPhotoUrl != null) userProfileData["photoUrl"] = finalPhotoUrl

                                firestore.collection("users").document(currentUser.uid)
                                    .set(userProfileData, SetOptions.merge()).await()

                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    Toast.makeText(context, "Đã lưu hồ sơ!", Toast.LENGTH_SHORT).show()
                                    onSaveClick()
                                }

                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
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
                TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            dateOfBirth = formatter.format(Date(it))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = datePickerState) }
    }
}

private fun Context.compressImage(uri: Uri): ByteArray {
    val inputStream = this.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val outputStream = ByteArrayOutputStream()
    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return outputStream.toByteArray()
}
