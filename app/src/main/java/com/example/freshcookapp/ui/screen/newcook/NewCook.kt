package com.example.freshcookapp.ui.screen.newcook

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.UnderlineTextField
import com.example.freshcookapp.ui.theme.Cinnabar400
import com.example.freshcookapp.ui.theme.Cinnabar50
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.White
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.example.freshcookapp.ui.screen.filter.DifficultyChip
import com.example.freshcookapp.ui.screen.filter.FilterChip

@Composable
fun NewCook(onBackClick: () -> Unit) {
    // --- STATE CHÍNH ---
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }

    val ingredients = remember {
        mutableStateListOf(
            Ingredient(), Ingredient()
        )
    }
    val instructions = remember {
        mutableStateListOf(
            Instruction(stepNumber = 1),
            Instruction(stepNumber = 2)
        )
    }

    // ⭐ State cho ảnh đại diện món
    var recipeImageUri by remember { mutableStateOf<Uri?>(null) }

    // ⭐ State cho hashtag
    var hashtagInput by remember { mutableStateOf("") }
    val hashtagList = remember { mutableStateListOf<String>() }

    // ⭐ State cho độ khó ("Dễ" / "Trung" / "Khó")
    var difficulty by remember { mutableStateOf("Dễ") }

    val context = LocalContext.current
    val app = context.applicationContext as FreshCookAppRoom
    val db = remember { AppDatabase.getDatabase(app) }
    val repo = remember { RecipeRepository(db) }
    val viewModel = remember { NewCookViewModel(repo) }
    val scope = rememberCoroutineScope()

    var isSaved by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Step-image picker state
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
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            bitmap?.let { b ->
                val bytes = java.io.ByteArrayOutputStream()
                b.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    b,
                    "step_image_${System.currentTimeMillis()}",
                    null
                )
                val uri = path.toUri()
                currentPickingStep?.let { idx ->
                    instructions[idx] = instructions[idx].copy(imageUrl = uri.toString())
                }
            }
            currentPickingStep = null
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission(),
        onResult = { granted ->
            if (granted) {
                cameraStepLauncher.launch(null)
            } else {
                scope.launch { snackbarHostState.showSnackbar("Cần quyền camera để chụp ảnh") }
            }
        }
    )

    // Helper function to parse cookTime to minutes
    fun parseCookTime(timeStr: String): Int? {
        val s = timeStr.trim().lowercase()
        if (s.isEmpty()) return null

        val normalized = s
            .replace("tieng", "tiếng")
            .replace("gio", "giờ")
            .replace("phut", "phút")

        val hourMinuteRegex = Regex("(\\d+)\\s*(giờ|tiếng|h)\\s*(\\d+)\\s*(phút|p|min|m)?")
        val compactHRegex = Regex("^(\\d+)h(\\d+)")
        val hourOnlyRegex = Regex("^(\\d+)\\s*(giờ|tiếng|h)$")
        val minuteOnlyRegex = Regex("^(\\d+)\\s*(phút|p|min|m)?$")

        hourMinuteRegex.find(normalized)?.let {
            val hours = it.groupValues[1].toIntOrNull() ?: 0
            val minutes = it.groupValues[3].toIntOrNull() ?: 0
            return hours * 60 + minutes
        }
        compactHRegex.find(normalized)?.let {
            val hours = it.groupValues[1].toIntOrNull() ?: 0
            val minutes = it.groupValues[2].toIntOrNull() ?: 0
            return hours * 60 + minutes
        }
        hourOnlyRegex.find(normalized)?.let {
            val hours = it.groupValues[1].toIntOrNull() ?: 0
            return hours * 60
        }
        minuteOnlyRegex.find(normalized)?.let {
            val minutes = it.groupValues[1].toIntOrNull() ?: 0
            return minutes
        }

        return normalized.toIntOrNull()
    }

    fun parsePeople(peopleStr: String): Int? {
        return Regex("(\\d+)").find(peopleStr)?.groups?.get(1)?.value?.toIntOrNull()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            if (!isSaved) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = {
                            val parsedMinutes = parseCookTime(cookTime)
                            if (parsedMinutes == null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Không hiểu định dạng thời gian. Ví dụ: '3 tiếng', '1h30', '90 phút'"
                                    )
                                }
                                return@Button
                            }

                            scope.launch {
                                viewModel.saveRecipe(
                                    name = recipeName,
                                    description = description,
                                    timeCookMinutes = parsedMinutes,
                                    people = parsePeople(people) ?: 1,
                                    imageUri = recipeImageUri,                 // ⭐ ảnh đại diện
                                    hashtags = hashtagList.toList(),          // ⭐ hashtag
                                    difficultyUi = difficulty,               // ⭐ "Dễ"/"Trung"/"Khó"
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
                                    onError = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Lỗi lưu công thức: ${it.message}")
                                        }
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cinnabar500,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Text(
                            text = "Lên sóng",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                /** ẢNH MÓN ĂN */
                item {
                    RecipeImagePicker(
                        imageUri = recipeImageUri,
                        onImageChanged = { recipeImageUri = it }
                    )
                }

                /** TÊN MÓN + MÔ TẢ + THỜI GIAN */
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(16.dp))
                        UnderlineTextField(
                            value = recipeName,
                            onValueChange = { recipeName = it },
                            placeholder = "Tên món: Món canh bí ngon nhất",
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            placeholderStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        )

                        Spacer(Modifier.height(16.dp))
                        UnderlineTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Hãy chia sẻ với mọi người về món này của bạn nhé..."
                        )

                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thời gian nấu",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            UnderlineTextField(
                                value = cookTime,
                                onValueChange = { cookTime = it },
                                placeholder = "1 tiếng 30 phút",
                                modifier = Modifier.width(160.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Số lượng người ăn:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            UnderlineTextField(
                                value = people,
                                onValueChange = { people = it },
                                placeholder = "2 người",
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }

                /** HASHTAG + DIFFICULTY */
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(20.dp))

                        Text(
                            "Hashtag",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        UnderlineTextField(
                            value = hashtagInput,
                            onValueChange = { hashtagInput = it },
                            placeholder = "Gõ vào hashtag...",
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (hashtagInput.isNotBlank()) {
                                        hashtagList.add(hashtagInput.trim())
                                        hashtagInput = ""
                                    }
                                }
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )

                        Spacer(Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            hashtagList.forEach { tag ->
                                FilterChip(text = tag) {
                                    hashtagList.remove(tag)
                                }
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        Text(
                            "Độ khó",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("Dễ", "Trung", "Khó").forEach { level ->
                                DifficultyChip(
                                    text = level,
                                    isSelected = difficulty == level,
                                    onClick = { difficulty = level }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }

                /** NGUYÊN LIỆU */
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Nguyên Liệu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
                itemsIndexed(ingredients) { index, ingredient ->
                    ScreenContainer {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cinnabar50)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Nguyên liệu ${index + 1}",
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { ingredients.removeAt(index) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa nguyên liệu")
                                }
                            }

                            UnderlineTextField(
                                value = ingredient.name,
                                onValueChange = { new -> ingredients[index] = ingredient.copy(name = new) },
                                placeholder = "Tên nguyên liệu"
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                UnderlineTextField(
                                    value = ingredient.quantity,
                                    onValueChange = { new -> ingredients[index] = ingredient.copy(quantity = new) },
                                    placeholder = "Số lượng",
                                    modifier = Modifier.weight(1f)
                                )
                                UnderlineTextField(
                                    value = ingredient.unit,
                                    onValueChange = { new -> ingredients[index] = ingredient.copy(unit = new) },
                                    placeholder = "Đơn vị",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            UnderlineTextField(
                                value = ingredient.notes,
                                onValueChange = { new -> ingredients[index] = ingredient.copy(notes = new) },
                                placeholder = "Ghi chú thêm (nếu có)"
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
                item {
                    ScreenContainer(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { ingredients.add(Ingredient()) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Cinnabar400
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Thêm nguyên liệu", color = Cinnabar400)
                        }
                    }
                }

                /** CÁCH LÀM */
                item {
                    ScreenContainer {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Cách Làm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                itemsIndexed(instructions) { index, instruction ->
                    ScreenContainer {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cinnabar50)
                                .padding(12.dp)
                        ) {
                            // --- Header: Số bước + nút xóa ---
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Bước ${index + 1}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                IconButton(onClick = { instructions.removeAt(index) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa bước")
                                }
                            }

                            // --- Mô tả bước ---
                            UnderlineTextField(
                                value = instruction.description,
                                onValueChange = { new ->
                                    instructions[index] = instruction.copy(description = new)
                                },
                                placeholder = "Mô tả bước nấu"
                            )

                            // --- Ảnh minh họa ---
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF2F2F2))
                                    .clickable {
                                        // mở dialog chọn ảnh cho bước
                                        currentPickingStep = index
                                        showStepDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (instruction.imageUrl.isNotEmpty()) {
                                    // Nếu có ảnh
                                    Image(
                                        painter = rememberAsyncImagePainter(instruction.imageUrl),
                                        contentDescription = "Ảnh bước ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_camera),
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            "Thêm ảnh minh họa",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }

                item {
                    ScreenContainer {
                        TextButton(
                            onClick = {
                                val nextStep = instructions.size + 1
                                instructions.add(Instruction(stepNumber = nextStep))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Cinnabar400
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Thêm bước", color = Cinnabar400)
                        }
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    if (showStepDialog) {
        ShowImageSourceDialog(
            onPickGallery = {
                galleryStepLauncher.launch("image/*")
                showStepDialog = false
            },
            onTakePhoto = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                showStepDialog = false
            }
        )
    }
}

/**
 * RecipeImagePicker: nhận imageUri từ ngoài & callback onImageChanged
 */
@Composable
fun RecipeImagePicker(
    imageUri: Uri?,
    onImageChanged: (Uri?) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onImageChanged(uri) }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                val bytes = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    bitmap,
                    "captured_image_${System.currentTimeMillis()}",
                    null
                )
                onImageChanged(path.toUri())
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Bạn cần cấp quyền camera", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Cinnabar50)
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    "Đăng tải hình đại diện món ăn",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showDialog) {
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
}

@Composable
fun ShowImageSourceDialog(
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Chọn ảnh món ăn") },
            text = { Text("Bạn muốn chụp ảnh mới hay chọn từ thư viện?") },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    onTakePhoto()
                }) {
                    Text("Chụp ảnh")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    onPickGallery()
                }) {
                    Text("Chọn ảnh")
                }
            }
        )
    }
}