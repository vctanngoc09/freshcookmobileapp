package com.example.freshcookapp.ui.screen.newcook

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import coil.size.Precision
import coil.request.CachePolicy
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.UnderlineTextField
import com.example.freshcookapp.ui.screen.filter.DifficultyChip
import com.example.freshcookapp.ui.screen.filter.FilterChip
import com.example.freshcookapp.ui.theme.Cinnabar400
import com.example.freshcookapp.ui.theme.Cinnabar50
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.White
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

// Dùng List (Immutable) để Compose nhận diện thay đổi tốt hơn
data class InstructionUiState(
    var description: String = "",
    var imageUris: List<Uri> = emptyList()
)

@Composable
fun NewCook(onBackClick: () -> Unit) {
    // --- STATE CHÍNH ---
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var totalCookMinutes by remember { mutableIntStateOf(0) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var people by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var categoryList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val ingredients = remember { mutableStateListOf(Ingredient()) }
    // Hỗ trợ nhiều ảnh trong 1 bước
    val instructionsUi = remember { mutableStateListOf(InstructionUiState()) }

    var recipeImageUri by remember { mutableStateOf<Uri?>(null) }
    var recipeVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoName by remember { mutableStateOf("") }

    var hashtagInput by remember { mutableStateOf("") }
    val hashtagList = remember { mutableStateListOf<String>() }
    var difficulty by remember { mutableStateOf("Dễ") }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    val viewModel = remember { NewCookViewModel(repo) }
    val scope = rememberCoroutineScope()

    var isSaved by remember { mutableStateOf(false) }
    val isUploading by viewModel.isUploading
    val snackbarHostState = remember { SnackbarHostState() }

    // --- STATE PREVIEW ---
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    var previewVideoUri by remember { mutableStateOf<Uri?>(null) }

    // --- IMAGE & VIDEO VARIABLES ---
    var tempStepUri by remember { mutableStateOf<Uri?>(null) }
    var currentPickingStepIndex by remember { mutableStateOf<Int?>(null) }
    var showStepDialog by remember { mutableStateOf(false) }
    var showVideoSourceDialog by remember { mutableStateOf(false) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    // --- LAUNCHERS ---
    val pickVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            if (checkFileSize(context, it, 50)) {
                recipeVideoUri = it
                videoName = getFileName(context, it)
            } else scope.launch { snackbarHostState.showSnackbar("Video quá lớn > 50MB") }
        }
    }
    val recordVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && tempVideoUri != null) {
            if (checkFileSize(context, tempVideoUri!!, 50)) {
                recipeVideoUri = tempVideoUri
                videoName = "Video vừa quay"
            } else {
                scope.launch { snackbarHostState.showSnackbar("Video quá lớn") }
                recipeVideoUri = null
            }
        }
    }
    val galleryStepLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { u ->
            currentPickingStepIndex?.let { idx ->
                val currentList = instructionsUi[idx].imageUris
                val newList = currentList + u
                instructionsUi[idx] = instructionsUi[idx].copy(imageUris = newList)
            }
        }
        currentPickingStepIndex = null
    }
    val cameraStepLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempStepUri != null) {
            currentPickingStepIndex?.let { idx ->
                val currentList = instructionsUi[idx].imageUris
                val newList = currentList + tempStepUri!!
                instructionsUi[idx] = instructionsUi[idx].copy(imageUris = newList)
            }
        }
        currentPickingStepIndex = null
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            tempStepUri = uri
            cameraStepLauncher.launch(uri)
        } else scope.launch { snackbarHostState.showSnackbar("Cần quyền camera") }
    }
    val videoCameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createVideoUri(context)
            tempVideoUri = uri
            recordVideoLauncher.launch(uri)
        } else scope.launch { snackbarHostState.showSnackbar("Cần quyền camera") }
    }

    fun parsePeople(peopleStr: String): Int? = Regex("(\\d+)").find(peopleStr)?.groups?.get(1)?.value?.toIntOrNull()

    LaunchedEffect(true) {
        FirebaseFirestore.getInstance().collection("categories").get().addOnSuccessListener { snapshot ->
            categoryList = snapshot.documents.map { doc -> doc.id to (doc.getString("name") ?: "") }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // ⭐ Dark Mode
            .clickable { focusManager.clearFocus() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
        // HEADER BUTTON
            if (!isSaved) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            if (!isUploading) {
                                if (recipeName.isBlank()) { scope.launch { snackbarHostState.showSnackbar("Nhập tên món ăn") }; return@Button }
                                if (totalCookMinutes <= 0) { scope.launch { snackbarHostState.showSnackbar("Chọn thời gian nấu") }; return@Button }
                                scope.launch {
                                    viewModel.saveRecipe(
                                        name = recipeName, description = description, timeCook = totalCookMinutes,
                                        people = parsePeople(people) ?: 1, imageUri = recipeImageUri, videoUri = recipeVideoUri,
                                        hashtags = hashtagList.toList(), difficultyUi = difficulty, categoryId = selectedCategoryId,
                                        ingredients = ingredients.filter { it.name.isNotBlank() }, instructionsUi = instructionsUi,
                                        onSuccess = { isSaved = true; scope.launch { snackbarHostState.showSnackbar("Thành công!"); delay(1000); onBackClick() } },
                                        onError = { scope.launch { snackbarHostState.showSnackbar("Lỗi: ${it.message}") } }
                                    )
                                }
                            }
                        },
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUploading) MaterialTheme.colorScheme.surfaceVariant else Cinnabar500,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) { if (isUploading) CircularProgressIndicator(color = White, modifier = Modifier.size(18.dp)) else Text("Lên sóng", style = MaterialTheme.typography.labelLarge) }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // 1. ẢNH ĐẠI DIỆN (Đã tích hợp Coil Nét Căng)
                item {
                    RecipeImagePicker(
                        imageUri = recipeImageUri,
                        onImageChanged = { recipeImageUri = it },
                        onImageClick = { if (recipeImageUri != null) previewImageUri = recipeImageUri }
                    )
                }

                // 1.1 VIDEO PICKER
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().border(
                                1.dp,
                                if (recipeVideoUri != null) Cinnabar500 else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(8.dp)
                            )
                                .background(
                                    if (recipeVideoUri != null) Cinnabar50 else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    focusManager.clearFocus()
                                    if(recipeVideoUri != null) previewVideoUri = recipeVideoUri else showVideoSourceDialog = true
                                }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = if(recipeVideoUri == null) Arrangement.Center else Arrangement.Start) {
                                if (recipeVideoUri == null) {
                                    Icon(Icons.Default.VideoCall, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(8.dp)); Text("Thêm Video hướng dẫn", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Icon(Icons.Default.PlayCircle, null, tint = Cinnabar500, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) { Text("Video đã chọn (Nhấn xem)", color = Cinnabar500, fontWeight = FontWeight.Bold); Text(videoName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                                    IconButton(onClick = { recipeVideoUri = null; videoName = "" }) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }

                // 2. THÔNG TIN CƠ BẢN
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(16.dp))
                        UnderlineTextField(value = recipeName, onValueChange = { recipeName = it }, placeholder = "Tên món ăn", textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(16.dp))
                        UnderlineTextField(value = description, onValueChange = { description = it }, placeholder = "Mô tả món ăn...")
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth().clickable {
                            focusManager.clearFocus()
                            showTimeDialog = true
                        }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Thời gian nấu", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, null, tint = Cinnabar500, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                                Text(if (totalCookMinutes > 0) formatMinutesToHours(totalCookMinutes) else "Chọn", fontWeight = FontWeight.Bold, color = if (totalCookMinutes > 0) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Khẩu phần:", style = MaterialTheme.typography.bodyMedium)
                            UnderlineTextField(value = people, onValueChange = { people = it }, placeholder = "2", modifier = Modifier.width(100.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }
                }

                // 3. HASHTAG & ĐỘ KHÓ
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(20.dp)); Text("Hashtag & Độ khó", fontWeight = FontWeight.Bold)
                        UnderlineTextField(value = hashtagInput, onValueChange = { hashtagInput = it }, placeholder = "#ngon #nhanh", keyboardActions = KeyboardActions(onDone = { if (hashtagInput.isNotBlank()) { hashtagList.add(hashtagInput.trim()); hashtagInput = "" } }), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { hashtagList.forEach { tag -> FilterChip(text = tag) { hashtagList.remove(tag) } } }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            listOf("Dễ", "Trung bình", "Khó").forEachIndexed { i, l -> DifficultyChip(text = l, isSelected = difficulty == l, onClick = { difficulty = l }); if (i < 2) Spacer(Modifier.width(12.dp)) }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Danh mục", fontWeight = FontWeight.Bold)
                        if (categoryList.isNotEmpty()) {
                            Column { categoryList.forEach { (id, name) -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedCategoryId = id }) { RadioButton(selected = selectedCategoryId == id, onClick = { selectedCategoryId = id }, colors = RadioButtonDefaults.colors(
                                selectedColor = Cinnabar500,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )); Text(name) } } }
                        } else Text("Đang tải danh mục...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // 4. NGUYÊN LIỆU (CARD UI)
                item { ScreenContainer { Spacer(Modifier.height(20.dp)); Text("Nguyên Liệu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)) } }
                itemsIndexed(ingredients) { index, ingredient ->
                    IngredientCardItem(
                        index = index,
                        ingredient = ingredient,
                        onUpdate = { new -> ingredients[index] = new },
                        onRemove = { ingredients.removeAt(index) }
                    )
                }
                item { ScreenContainer { TextButton(onClick = { ingredients.add(Ingredient()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Icon(Icons.Default.Add, null, tint = Cinnabar400); Text("Thêm dòng", color = Cinnabar400) } } }

                // 5. CÁCH LÀM
                item { ScreenContainer { Spacer(Modifier.height(16.dp)); Text("Cách Làm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)) } }
                itemsIndexed(instructionsUi) { index, instructionUi ->
                    InstructionCardItem(
                        index = index,
                        uiState = instructionUi,
                        onUpdateDescription = { new -> instructionsUi[index] = instructionUi.copy(description = new) },
                        onRemoveStep = { instructionsUi.removeAt(index) },
                        onAddImage = {
                            focusManager.clearFocus()
                            currentPickingStepIndex = index;
                            showStepDialog = true
                        },
                        onRemoveImage = { uri ->
                            val currentList = instructionsUi[index].imageUris
                            val newList = currentList - uri
                            instructionsUi[index] = instructionsUi[index].copy(imageUris = newList)
                        },
                        onViewImage = { uri -> previewImageUri = uri }
                    )
                }
                item { ScreenContainer { TextButton(onClick = { instructionsUi.add(InstructionUiState()) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null, tint = Cinnabar400); Text("Thêm bước thực hiện", color = Cinnabar400) }; Spacer(Modifier.height(40.dp)) } }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
    }

    // DIALOGS
    if (previewImageUri != null) ImagePreviewDialog(imageUri = previewImageUri!!, onDismiss = { previewImageUri = null })
    if (previewVideoUri != null) VideoPreviewDialog(videoUri = previewVideoUri!!, onDismiss = { previewVideoUri = null })
    if (showStepDialog) ShowImageSourceDialog(onPickGallery = { galleryStepLauncher.launch("image/*"); showStepDialog = false }, onTakePhoto = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA); showStepDialog = false })
    if (showVideoSourceDialog) ShowVideoSourceDialog(onPickGallery = { pickVideoLauncher.launch("video/*"); showVideoSourceDialog = false }, onRecord = { videoCameraPermissionLauncher.launch(Manifest.permission.CAMERA); showVideoSourceDialog = false }, onDismiss = { showVideoSourceDialog = false })
    if (showTimeDialog) DurationPickerDialog(initialMinutes = totalCookMinutes, onDismiss = { showTimeDialog = false }, onConfirm = { minutes -> totalCookMinutes = minutes; showTimeDialog = false })
}

// --- ITEM: THẺ NGUYÊN LIỆU ---
@Composable
fun IngredientCardItem(index: Int, ingredient: Ingredient, onUpdate: (Ingredient) -> Unit, onRemove: () -> Unit) {
    val commonUnits = listOf("g", "kg", "ml", "l", "muỗng", "cái", "trái", "tép")
    ScreenContainer {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface  // ⭐ tự đổi theo Dark/Light
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant           // ⭐ viền đúng Material
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalDining,
                        null,
                        tint = Cinnabar500,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "Nguyên liệu ${index + 1}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.weight(1f))

                    Icon(
                        Icons.Default.Delete,
                        "Xóa",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onRemove() }
                    )
                }

                Spacer(Modifier.height(8.dp))

                UnderlineTextField(
                    value = ingredient.name,
                    onValueChange = { onUpdate(ingredient.copy(name = it)) },
                    placeholder = "Tên nguyên liệu (VD: Thịt)",
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        UnderlineTextField(
                            value = ingredient.quantity,
                            onValueChange = { onUpdate(ingredient.copy(quantity = it)) },
                            placeholder = "Số lượng",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        UnderlineTextField(
                            value = ingredient.unit,
                            onValueChange = { onUpdate(ingredient.copy(unit = it)) },
                            placeholder = "Đơn vị"
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(commonUnits) { unit ->
                        UnitChip(
                            text = unit,
                            isSelected = ingredient.unit == unit,
                            onClick = { onUpdate(ingredient.copy(unit = unit)) }
                        )
                    }
                }
            }
        }
    }
}

// --- ITEM: THẺ CÁCH LÀM ---
@Composable
fun InstructionCardItem(
    index: Int,
    uiState: InstructionUiState,
    onUpdateDescription: (String) -> Unit,
    onRemoveStep: () -> Unit,
    onAddImage: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onViewImage: (Uri) -> Unit
) {
    ScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {

            /** HEADER */
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Bước ${index + 1}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Cinnabar500
                    )
                )
                Icon(
                    Icons.Default.Close,
                    "Xóa",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onRemoveStep() }
                )
            }

            /** DESCRIPTION */
            UnderlineTextField(
                value = uiState.description,
                onValueChange = onUpdateDescription,
                placeholder = "Mô tả chi tiết bước này...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            /** IMAGE LIST */
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(110.dp)
            ) {

                /** DANH SÁCH ẢNH */
                items(uiState.imageUris) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .size(300)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onViewImage(uri) },
                            contentScale = ContentScale.Crop
                        )

                        /** NÚT XÓA ẢNH */
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    CircleShape
                                )
                                .size(20.dp)
                                .clickable { onRemoveImage(uri) }
                        )
                    }
                }

                /** NÚT THÊM ẢNH */
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)   // ⭐ Dark mode ok
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,    // ⭐ Dark mode viền dịu
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onAddImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Thêm ảnh",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            /** DIVIDER */
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant   // ⭐ Dark mode viền đẹp
            )
        }
    }
}

// --- CUSTOM CHIP ---
@Composable
fun UnitChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) Cinnabar500 else MaterialTheme.colorScheme.outlineVariant
        ),
        color = if (isSelected) Cinnabar50 else MaterialTheme.colorScheme.surface // ⭐ auto Light/Dark
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Cinnabar500 else MaterialTheme.colorScheme.onSurface
        )
    }
}


// --- DIALOGS & HELPERS ---
@Composable
fun ImagePreviewDialog(imageUri: Uri, onDismiss: () -> Unit) {

    val colors = MaterialTheme.colorScheme

    val closeBg = colors.surface.copy(alpha = 0.7f)
    val closeIcon = colors.onSurface

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        // Nền đen để ảnh nổi bật nhất (không theo theme)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .size(Size.ORIGINAL)
                        .precision(Precision.EXACT)
                        .build()
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 3f)
                            offset += pan
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            // CLOSE BUTTON theo theme app
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(closeBg, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = closeIcon
                )
            }
        }
    }
}

@Composable
fun VideoPreviewDialog(videoUri: Uri, onDismiss: () -> Unit) {

    val colors = MaterialTheme.colorScheme

    val closeBg = colors.surface.copy(alpha = 0.7f)
    val closeIcon = colors.onSurface

    Dialog(onDismissRequest = onDismiss) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Black)
        ) {

            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(videoUri)
                        setMediaController(MediaController(ctx))
                        start()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(closeBg, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = closeIcon
                )
            }
        }
    }
}

@Composable
fun DurationPickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableStateOf((initialMinutes / 60).toString()) }
    var minutes by remember { mutableStateOf((initialMinutes % 60).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                "Thời gian nấu",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },

        text = {
            Column {

                // INPUT ROW
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // HOURS
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.all { c -> c.isDigit() }) hours = it },
                        label = { Text("Giờ") },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,

                            focusedBorderColor = Cinnabar500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                            cursorColor = Cinnabar500,
                            focusedLabelColor = Cinnabar500,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // SEPARATOR
                    Text(
                        " : ",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // MINUTES
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minutes = it },
                        label = { Text("Phút") },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,

                            focusedBorderColor = Cinnabar500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                            cursorColor = Cinnabar500,
                            focusedLabelColor = Cinnabar500,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                // TOTAL TIME
                val totalMinutes = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)

                Text(
                    "Tổng: ${formatMinutesToHours(totalMinutes)}",
                    color = Cinnabar500,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    onConfirm((hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Cinnabar500)
            ) {
                Text("Xác nhận")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ShowVideoSourceDialog(onPickGallery: () -> Unit, onRecord: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Thêm Video") }, text = { Text("Chọn video từ thư viện hoặc quay mới?") }, confirmButton = { TextButton(onClick = onRecord) { Text("Quay video") } }, dismissButton = { TextButton(onClick = onPickGallery) { Text("Chọn thư viện") } })
}

@Composable
fun ShowImageSourceDialog(onPickGallery: () -> Unit, onTakePhoto: () -> Unit) {
    AlertDialog(onDismissRequest = {}, title = { Text("Chọn ảnh") }, text = { Text("Chụp mới hay chọn từ thư viện?") }, confirmButton = { TextButton(onClick = onTakePhoto) { Text("Chụp ảnh") } }, dismissButton = { TextButton(onClick = onPickGallery) { Text("Chọn ảnh") } })
}

// 🔥 CẬP NHẬT: COMPONENT IMAGE PICKER VỚI COIL SETTINGS "NÉT CĂNG" TỪ BẢN PULL
@Composable
fun RecipeImagePicker(
    imageUri: Uri?,
    onImageChanged: (Uri?) -> Unit,
    onImageClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val colors = MaterialTheme.colorScheme

    val pickerBg = colors.surfaceVariant.copy(alpha = 0.6f)
    val editBg = colors.surface.copy(alpha = 0.7f)
    val editIconColor = colors.onSurface

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> onImageChanged(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) onImageChanged(tempUri)
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val uri = createImageUri(context)
                tempUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Cần quyền camera", Toast.LENGTH_SHORT).show()
            }
        }

    val boxHeight = if (imageUri != null) 260.dp else 160.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(pickerBg)   // ⭐ CHẠY THEO THEME APP
            .clickable { if (imageUri == null) showDialog = true else onImageClick() },
        contentAlignment = Alignment.Center
    ) {

        /** KHI CHƯA CÓ ẢNH */
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painterResource(R.drawable.ic_camera),
                    null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    "Đăng tải hình đại diện",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        /** ĐÃ CÓ ẢNH */
        else {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .size(Size.ORIGINAL)
                    .precision(Precision.EXACT)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .crossfade(true)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // ⭐ EDIT BUTTON THEO THEME
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(editBg, CircleShape)
                    .clickable { showDialog = true }
                    .padding(8.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_camera),
                    null,
                    tint = editIconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    /** DIALOG CHỌN ẢNH */
    if (showDialog)
        ShowImageSourceDialog(
            onPickGallery = {
                galleryLauncher.launch("image/*")
                showDialog = false
            },
            onTakePhoto = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                showDialog = false
            }
        )
}

fun createImageUri(context: Context): Uri { val file = File.createTempFile("recipe_img_${System.currentTimeMillis()}", ".jpg", context.cacheDir); return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file) }
fun createVideoUri(context: Context): Uri { val file = File.createTempFile("recipe_video_${System.currentTimeMillis()}", ".mp4", context.cacheDir); return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file) }
fun checkFileSize(context: Context, uri: Uri, limitMB: Int): Boolean { val cursor = context.contentResolver.query(uri, null, null, null, null); cursor?.use { if (it.moveToFirst()) { val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE); if (sizeIndex != -1) return it.getLong(sizeIndex) <= limitMB * 1024 * 1024 } }; return true }
fun getFileName(context: Context, uri: Uri): String { var result: String? = null; if (uri.scheme == "content") { val cursor = context.contentResolver.query(uri, null, null, null, null); cursor?.use { if (it.moveToFirst()) { val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(index >= 0) result = it.getString(index) } } }; return result ?: "Video" }
fun formatMinutesToHours(totalMinutes: Int): String { val hours = totalMinutes / 60; val minutes = totalMinutes % 60; return when { hours > 0 && minutes > 0 -> "$hours giờ $minutes phút"; hours > 0 -> "$hours giờ"; else -> "$minutes phút" } }