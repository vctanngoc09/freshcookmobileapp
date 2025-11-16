package com.example.freshcookapp.ui.screen.newcook

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.freshcookapp.R
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import com.example.freshcookapp.ui.component.ScreenContainer
import com.example.freshcookapp.ui.component.SearchBar
import com.example.freshcookapp.ui.component.UnderlineTextField
import com.example.freshcookapp.ui.theme.Cinnabar100
import com.example.freshcookapp.ui.theme.Cinnabar200
import com.example.freshcookapp.ui.theme.Cinnabar400
import com.example.freshcookapp.ui.theme.Cinnabar50
import com.example.freshcookapp.ui.theme.Cinnabar500
import com.example.freshcookapp.ui.theme.White
import java.io.ByteArrayOutputStream
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshcookapp.FreshCookAppRoom
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.RecipeRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

@Composable
fun NewCook(onBackClick: () -> Unit){
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf(
        Ingredient(), Ingredient()
    ) }
    val instructions = remember {
        mutableStateListOf(
            Instruction(stepNumber = 1),
            Instruction(stepNumber = 2)
        )
    }

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
                val bytes = ByteArrayOutputStream()
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
                // show snackbar if denied
                scope.launch { snackbarHostState.showSnackbar("Cần quyền camera để chụp ảnh") }
            }
        }
    )

    // Helper function to parse cookTime to minutes
    fun parseCookTime(timeStr: String): Int? {
        val s = timeStr.trim().lowercase()
        if (s.isEmpty()) return null

        // Normalize common Vietnamese no-diacritic variants to unify matching
        val normalized = s
            .replace("tieng", "tiếng")
            .replace("gio", "giờ")
            .replace("phut", "phút")

        // Patterns to support many formats (including no-diacritic variants):
        // - "1 giờ 30 phút", "1 tiếng 30 phút"
        // - "1h30" or "1h 30"
        // - "90 phút" or just "90"
        // - "3 giờ" / "3 tiếng" / "3h" / "3 tieng"
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

        // last resort: try parse as plain number (assume minutes)
        return normalized.toIntOrNull()
    }

    // Helper function to parse people
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
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        Button(
                            onClick = {
                                val parsedMinutes = parseCookTime(cookTime)
                                if (parsedMinutes == null) {
                                    // show helpful message and do not save
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Không hiểu định dạng thời gian. Ví dụ: '3 tiếng', '1h30', '90 phút'")
                                    }
                                    return@Button
                                }

                                scope.launch {
                                    // show parsed result to help debug (so user sees what will be saved)
                                    snackbarHostState.showSnackbar("Thời gian được nhận dạng: ${'$'}{parsedMinutes} phút")

                                    viewModel.saveRecipe(
                                        name = recipeName,
                                        description = description,
                                        // đảm bảo không truyền null vào repository/firestore
                                        timeCookMinutes = parsedMinutes,
                                        people = parsePeople(people) ?: 1,
                                        imageUrl = null, // TODO: add imageUrl
                                        userId = 1L, // use Long
                                        categoryId = 1L, // use Long
                                        ingredients = ingredients.filter { it.name.isNotBlank() },
                                        instructions = instructions.filter { it.description.isNotBlank() },
                                        onSuccess = {
                                            // run UI actions in UI coroutine scope
                                            scope.launch {
                                                isSaved = true
                                                snackbarHostState.showSnackbar("Lưu công thức thành công!")
                                                delay(1000)
                                                onBackClick()
                                            }
                                        },
                                        onError = { /* TODO: handle error */ }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cinnabar500,   // nền nhạt như ảnh
                                contentColor = White            // chữ đen
                            ),
                            shape = RoundedCornerShape(10.dp), // bo tròn đều (hình viên thuốc)
                            contentPadding = PaddingValues(
                                horizontal = 16.dp, // giảm padding ngang (mặc định 24.dp)
                                vertical = 6.dp     // giảm padding dọc (mặc định 12.dp)
                            )
                        ) {
                            Text(
                                text = "Lên sóng",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }


                }
            }

            Spacer(modifier = Modifier.height(10.dp))


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                /** ẢNH MÓN ĂN */
                item {
                    RecipeImagePicker()
                }


                /** TÊN MÓN + MÔ TẢ + THỜI GIAN */
                item {
                    ScreenContainer {

                        // --- Ô “Tên món” to hơn ---
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

                        // --- Ô mô tả bình thường ---
                        Spacer(Modifier.height(16.dp))
                        UnderlineTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Hãy chia sẻ với mọi người về món này của bạn nhé. " +
                                    "Ai hay điều gì đã truyền cảm hứng cho bạn nấu nó? " +
                                    "Tại sao nó đặc biệt? Bạn thích thưởng thức nó theo cách nào?"
                        )

                        // --- Ô thời gian nấu bình thường ---
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
                                placeholder = "Tên nguyên liệu (VD: Bột mì)"
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
                                    placeholder = "Đơn vị (VD: gram, ml)",
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

        // Snackbar host at bottom center
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // Step image source dialog
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

@Composable
fun RecipeImagePicker() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) } // ✅ trạng thái bật/tắt dialog

    // --- Bộ chọn ảnh từ thư viện ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    // --- Bộ chụp ảnh bằng camera ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    bitmap,
                    "captured_image",
                    null
                )
                imageUri = path.toUri()
            }
        }
    )

    // --- UI chính ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Cinnabar100)
            .clickable {
                showDialog = true // ✅ chỉ bật cờ lên thôi
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = "Camera",
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
                contentDescription = "Recipe image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // --- Dialog chọn nguồn ảnh ---
    if (showDialog) {
        ShowImageSourceDialog(
            onPickGallery = {
                galleryLauncher.launch("image/*")
                showDialog = false
            },
            onTakePhoto = {
                cameraLauncher.launch(null)
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
