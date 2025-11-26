package com.example.freshcookapp.ui.screen.newcook

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
// --- CÁC IMPORT QUAN TRỌNG ĐỂ HIỂN THỊ ẢNH NÉT ---
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import coil.size.Precision
import coil.request.CachePolicy
// -------------------------------------------------
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.R
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
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

@Composable
fun NewCook(onBackClick: () -> Unit) {
    // --- STATE CHÍNH ---
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var categoryList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val ingredients = remember { mutableStateListOf(Ingredient(), Ingredient()) }
    val instructions = remember { mutableStateListOf(Instruction(stepNumber = 1), Instruction(stepNumber = 2)) }

    var recipeImageUri by remember { mutableStateOf<Uri?>(null) }
    var hashtagInput by remember { mutableStateOf("") }
    val hashtagList = remember { mutableStateListOf<String>() }
    var difficulty by remember { mutableStateOf("Dễ") }

    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    val viewModel = remember { NewCookViewModel(repo) }
    val scope = rememberCoroutineScope()

    var isSaved by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // --- CAMERA & ẢNH (STEP) ---
    var tempStepUri by remember { mutableStateOf<Uri?>(null) }
    var currentPickingStep by remember { mutableStateOf<Int?>(null) }
    var showStepDialog by remember { mutableStateOf(false) }

    val galleryStepLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { u ->
                currentPickingStep?.let { idx ->
                    instructions[idx] = instructions[idx].copy(imageUrl = u.toString())
                }
            }
            currentPickingStep = null
        }
    )

    val cameraStepLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempStepUri != null) {
                currentPickingStep?.let { idx ->
                    instructions[idx] = instructions[idx].copy(imageUrl = tempStepUri.toString())
                }
            }
            currentPickingStep = null
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val uri = createImageUri(context)
                tempStepUri = uri
                cameraStepLauncher.launch(uri)
            } else {
                scope.launch { snackbarHostState.showSnackbar("Cần quyền camera để chụp ảnh") }
            }
        }
    )

    // --- HELPER FUNCTIONS ---
    fun parseCookTime(timeStr: String): Int? {
        val s = timeStr.trim().lowercase()
        if (s.isEmpty()) return null
        val normalized = s.replace("tieng", "tiếng").replace("gio", "giờ").replace("phut", "phút")
        val hourMinuteRegex = Regex("(\\d+)\\s*(giờ|tiếng|h)\\s*(\\d+)\\s*(phút|p|min|m)?")
        val compactHRegex = Regex("^(\\d+)h(\\d+)")
        val hourOnlyRegex = Regex("^(\\d+)\\s*(giờ|tiếng|h)$")
        val minuteOnlyRegex = Regex("^(\\d+)\\s*(phút|p|min|m)?$")

        hourMinuteRegex.find(normalized)?.let { return (it.groupValues[1].toIntOrNull() ?: 0) * 60 + (it.groupValues[3].toIntOrNull() ?: 0) }
        compactHRegex.find(normalized)?.let { return (it.groupValues[1].toIntOrNull() ?: 0) * 60 + (it.groupValues[2].toIntOrNull() ?: 0) }
        hourOnlyRegex.find(normalized)?.let { return (it.groupValues[1].toIntOrNull() ?: 0) * 60 }
        minuteOnlyRegex.find(normalized)?.let { return it.groupValues[1].toIntOrNull() ?: 0 }
        return normalized.toIntOrNull()
    }

    fun parsePeople(peopleStr: String): Int? {
        return Regex("(\\d+)").find(peopleStr)?.groups?.get(1)?.value?.toIntOrNull()
    }

    LaunchedEffect(true) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("categories").get().addOnSuccessListener { snapshot ->
            categoryList = snapshot.documents.map { doc -> doc.id to (doc.getString("name") ?: "") }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
            if (!isSaved) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = {
                            val parsedMinutes = parseCookTime(cookTime)
                            if (parsedMinutes == null) {
                                scope.launch { snackbarHostState.showSnackbar("Không hiểu định dạng thời gian.") }
                                return@Button
                            }
                            scope.launch {
                                viewModel.saveRecipe(
                                    name = recipeName,
                                    description = description,
                                    timeCook = parsedMinutes,
                                    people = parsePeople(people) ?: 1,
                                    imageUri = recipeImageUri,
                                    hashtags = hashtagList.toList(),
                                    difficultyUi = difficulty,
                                    categoryId = selectedCategoryId,
                                    ingredients = ingredients.filter { it.name.isNotBlank() },
                                    instructions = instructions.filter { it.description.isNotBlank() },
                                    onSuccess = {
                                        isSaved = true
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Lưu công thức thành công!")
                                            delay(1000)
                                            onBackClick()
                                        }
                                    },
                                    onError = { scope.launch { snackbarHostState.showSnackbar("Lỗi: ${it.message}") } }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cinnabar500, contentColor = White),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) { Text("Lên sóng", style = MaterialTheme.typography.labelLarge) }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // 1. ẢNH ĐẠI DIỆN (Dùng Component Đã Fix Nét)
                item {
                    RecipeImagePicker(
                        imageUri = recipeImageUri,
                        onImageChanged = { recipeImageUri = it }
                    )
                }

                // 2. TÊN MÓN, MÔ TẢ...
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(16.dp))
                        UnderlineTextField(
                            value = recipeName,
                            onValueChange = { recipeName = it },
                            placeholder = "Tên món: Món canh bí ngon nhất",
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black),
                            placeholderStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                        )
                        Spacer(Modifier.height(16.dp))
                        UnderlineTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Hãy chia sẻ với mọi người về món này của bạn nhé..."
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Thời gian nấu", style = MaterialTheme.typography.bodyMedium)
                            UnderlineTextField(value = cookTime, onValueChange = { cookTime = it }, placeholder = "1 tiếng 30 phút", modifier = Modifier.width(160.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Số lượng người ăn:", style = MaterialTheme.typography.bodyMedium)
                            UnderlineTextField(value = people, onValueChange = { people = it }, placeholder = "2 người", modifier = Modifier.width(160.dp))
                        }
                    }
                }

                // 3. HASHTAG & CATEGORY
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(20.dp))
                        Text("Hashtag", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        UnderlineTextField(
                            value = hashtagInput,
                            onValueChange = { hashtagInput = it },
                            placeholder = "Gõ vào hashtag...",
                            keyboardActions = KeyboardActions(onDone = { if (hashtagInput.isNotBlank()) { hashtagList.add(hashtagInput.trim()); hashtagInput = "" } }),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            hashtagList.forEach { tag -> FilterChip(text = tag) { hashtagList.remove(tag) } }
                        }
                        Spacer(Modifier.height(28.dp))
                        Text("Độ khó", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("Dễ", "Trung", "Khó").forEach { level ->
                                DifficultyChip(text = level, isSelected = difficulty == level, onClick = { difficulty = level })
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (categoryList.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                categoryList.forEach { (id, name) ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedCategoryId = id }) {
                                        RadioButton(selected = selectedCategoryId == id, onClick = { selectedCategoryId = id }, colors = RadioButtonDefaults.colors(selectedColor = Cinnabar500))
                                        Text(name)
                                    }
                                }
                            }
                        } else Text("Đang tải danh mục...")
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // 4. NGUYÊN LIỆU
                item { ScreenContainer { Text("Nguyên Liệu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)) } }
                itemsIndexed(ingredients) { index, ingredient ->
                    ScreenContainer {
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Cinnabar50).padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Nguyên liệu ${index + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                IconButton(onClick = { ingredients.removeAt(index) }) { Icon(Icons.Default.Close, "Xóa") }
                            }
                            UnderlineTextField(value = ingredient.name, onValueChange = { new -> ingredients[index] = ingredient.copy(name = new) }, placeholder = "Tên nguyên liệu")
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                UnderlineTextField(value = ingredient.quantity, onValueChange = { new -> ingredients[index] = ingredient.copy(quantity = new) }, placeholder = "Số lượng", modifier = Modifier.weight(1f))
                                UnderlineTextField(value = ingredient.unit, onValueChange = { new -> ingredients[index] = ingredient.copy(unit = new) }, placeholder = "Đơn vị", modifier = Modifier.weight(1f))
                            }
                            UnderlineTextField(value = ingredient.notes, onValueChange = { new -> ingredients[index] = ingredient.copy(notes = new) }, placeholder = "Ghi chú")
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
                item { ScreenContainer { TextButton(onClick = { ingredients.add(Ingredient()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Icon(Icons.Default.Add, null, tint = Cinnabar400); Spacer(Modifier.width(4.dp)); Text("Thêm nguyên liệu", color = Cinnabar400) } } }

                // 5. CÁCH LÀM (Sửa hiển thị ảnh Nét)
                item { ScreenContainer { Spacer(Modifier.height(16.dp)); Text("Cách Làm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)) } }
                itemsIndexed(instructions) { index, instruction ->
                    ScreenContainer {
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Cinnabar50).padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Bước ${index + 1}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                                IconButton(onClick = { instructions.removeAt(index) }) { Icon(Icons.Default.Close, "Xóa") }
                            }
                            UnderlineTextField(value = instruction.description, onValueChange = { new -> instructions[index] = instruction.copy(description = new) }, placeholder = "Mô tả bước nấu")

                            Box(
                                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF2F2F2))
                                    .clickable {
                                        currentPickingStep = index
                                        showStepDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (instruction.imageUrl.isNotEmpty()) {
                                    // --- CẤU HÌNH COIL ĐỂ HIỂN THỊ ẢNH NÉT ---
                                    val painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(instruction.imageUrl)
                                            .size(Size.ORIGINAL) // Ép lấy ảnh gốc
                                            .crossfade(true)
                                            .build()
                                    )
                                    Image(painter = painter, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(painterResource(R.drawable.ic_camera), null, tint = Color.Gray, modifier = Modifier.size(28.dp))
                                        Text("Thêm ảnh minh họa", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
                item { ScreenContainer { TextButton(onClick = { instructions.add(Instruction(stepNumber = instructions.size + 1)) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null, tint = Cinnabar400); Spacer(Modifier.width(4.dp)); Text("Thêm bước", color = Cinnabar400) }; Spacer(Modifier.height(40.dp)) } }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
    }

    if (showStepDialog) {
        ShowImageSourceDialog(
            onPickGallery = { galleryStepLauncher.launch("image/*"); showStepDialog = false },
            onTakePhoto = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA); showStepDialog = false }
        )
    }
}

/**
 * COMPONENT CHỌN ẢNH ĐẠI DIỆN (ĐÃ FIX NÉT CĂNG)
 */
@Composable
fun RecipeImagePicker(imageUri: Uri?, onImageChanged: (Uri?) -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> onImageChanged(uri) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) onImageChanged(tempUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        } else Toast.makeText(context, "Cần quyền camera", Toast.LENGTH_SHORT).show()
    }

    // --- ĐIỀU CHỈNH CHIỀU CAO ĐỂ TRÁNH ZOOM VỠ ẢNH ---
    val boxHeight = if (imageUri != null) 260.dp else 160.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(boxHeight) // <--- Chiều cao linh hoạt
            .clip(RoundedCornerShape(12.dp))
            .background(Cinnabar50)
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(R.drawable.ic_camera), null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                Text("Đăng tải hình đại diện món ăn", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            // --- CẤU HÌNH COIL NÉT CĂNG ---
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .size(Size.ORIGINAL)       // Kích thước gốc
                    .precision(Precision.EXACT) // Hiển thị chính xác pixel
                    .memoryCachePolicy(CachePolicy.DISABLED) // (Tùy chọn) Không cache để luôn thấy ảnh mới
                    .crossfade(true)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Icon nhỏ nhắc người dùng có thể đổi ảnh
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).background(Color.Black.copy(0.5f), CircleShape).padding(8.dp)) {
                Icon(painterResource(R.drawable.ic_camera), null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showDialog) {
        ShowImageSourceDialog(
            onPickGallery = { galleryLauncher.launch("image/*"); showDialog = false },
            onTakePhoto = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA); showDialog = false }
        )
    }
}

@Composable
fun ShowImageSourceDialog(onPickGallery: () -> Unit, onTakePhoto: () -> Unit) {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Chọn ảnh món ăn") },
            text = { Text("Bạn muốn chụp ảnh mới hay chọn từ thư viện?") },
            confirmButton = { TextButton(onClick = { openDialog.value = false; onTakePhoto() }) { Text("Chụp ảnh") } },
            dismissButton = { TextButton(onClick = { openDialog.value = false; onPickGallery() }) { Text("Chọn ảnh") } }
        )
    }
}

fun createImageUri(context: Context): Uri {
    val file = File.createTempFile("recipe_img_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}