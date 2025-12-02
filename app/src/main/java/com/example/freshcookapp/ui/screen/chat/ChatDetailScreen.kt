package com.example.freshcookapp.ui.screen.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.freshcookapp.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val currentChat by viewModel.currentChat.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isOtherUserTyping by viewModel.isOtherUserTyping.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var showImageOptions by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 🔥 SỬA: Load chat info trước rồi mới lấy thông tin user
    LaunchedEffect(chatId) {
        viewModel.loadChatMessages(chatId)
    }

    // 🔥 SỬA: Lấy thông tin user từ currentChat (được cập nhật bởi loadChatMessages)
    val otherUser = currentChat?.let { viewModel.getOtherUser(it) }
    val otherUserName = otherUser?.get("username") as? String
        ?: otherUser?.get("name") as? String
        ?: otherUser?.get("displayName") as? String
        ?: "Loading..."
    val otherUserPhoto = otherUser?.get("photoUrl") as? String
        ?: otherUser?.get("profileImage") as? String
        ?: otherUser?.get("avatar") as? String

    // 🔥 THÊM: Fallback - Load thông tin chi tiết từ Firebase nếu cần
    var firebaseUserName by remember { mutableStateOf<String?>(null) }
    var firebaseUserPhoto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentChat) {
        currentChat?.let { chat ->
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val otherUserId = chat.participantIds.firstOrNull { it != currentUserId }

            Log.d("ChatDetailScreen", "Current user ID: $currentUserId")
            Log.d("ChatDetailScreen", "Participant IDs: ${chat.participantIds}")
            Log.d("ChatDetailScreen", "Other user ID: $otherUserId")
            Log.d("ChatDetailScreen", "Participants map: ${chat.participants}")

            if (otherUserId != null) {
                try {
                    Log.d("ChatDetailScreen", "Loading user info from Firestore for: $otherUserId")
                    val userDoc = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(otherUserId)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        Log.d("ChatDetailScreen", "User doc exists: ${userDoc.data}")

                        // Thử nhiều key khác nhau cho tên
                        val userName = userDoc.getString("username")
                            ?: userDoc.getString("name")
                            ?: userDoc.getString("displayName")
                            ?: userDoc.getString("fullName")

                        // Thử nhiều key khác nhau cho ảnh
                        val userPhoto = userDoc.getString("profileImage")
                            ?: userDoc.getString("photoUrl")
                            ?: userDoc.getString("avatar")
                            ?: userDoc.getString("profilePicture")

                        // Cập nhật nếu tìm thấy giá trị hợp lệ
                        if (!userName.isNullOrBlank()) {
                            firebaseUserName = userName
                            Log.d("ChatDetailScreen", "✅ Updated name from Firestore: $userName")
                        }

                        if (!userPhoto.isNullOrBlank()) {
                            firebaseUserPhoto = userPhoto
                            Log.d("ChatDetailScreen", "✅ Updated photo from Firestore: $userPhoto")
                        }
                    } else {
                        Log.w("ChatDetailScreen", "❌ User document does not exist for ID: $otherUserId")
                    }
                } catch (e: Exception) {
                    Log.e("ChatDetailScreen", "❌ Error loading user info: ${e.message}", e)
                }
            } else {
                Log.w("ChatDetailScreen", "❌ Could not find other user ID in participantIds: ${chat.participantIds}")
            }
        }
    }

    // 🔥 SỬA: Sử dụng giá trị từ Firebase nếu có, nếu không dùng từ Chat object
    val displayName = firebaseUserName ?: otherUserName
    val displayPhoto = firebaseUserPhoto ?: otherUserPhoto

    // 🔥 THÊM: Image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadAndSendImage(chatId, it) }
    }

    // Auto scroll xuống khi có tin nhắn mới
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Lắng nghe typing status
    LaunchedEffect(messageText) {
        viewModel.onTypingTextChanged(chatId, messageText)
    }

    // 🔥 THÊM: Detect khi scroll đến đầu list → load more
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                if (index == 0 && canLoadMore && !isLoadingMore && messages.isNotEmpty()) {
                    viewModel.loadMoreMessages(chatId)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = displayPhoto?.ifEmpty { null },
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = displayName ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (isOtherUserTyping) {
                                Text(
                                    text = "đang nhập...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    // 🔥 THÊM: Upload progress indicator
                    if (isUploadingImage) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🔥 THÊM: Image button
                        IconButton(onClick = { showImageOptions = true }) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Chọn ảnh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập tin nhắn...") },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 🔥 ICON GỬI - CẢI THIỆN
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(chatId, messageText.trim())
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank() && !isUploadingImage,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (messageText.isNotBlank() && !isUploadingImage)
                                        MaterialTheme.colorScheme.primary
                                    else Color.LightGray,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Gửi",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && messages.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "Có lỗi xảy ra",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Đóng")
                        }
                    }
                }
                messages.isEmpty() -> {
                    Text(
                        text = "Chưa có tin nhắn nào\nHãy gửi tin nhắn đầu tiên!",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 🔥 THÊM: Loading indicator khi load more
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }

                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == viewModel.getCurrentUserId(),
                                onDelete = {
                                    viewModel.deleteMessage(chatId, message.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 🔥 THÊM: Bottom sheet cho chọn ảnh từ gallery hoặc camera
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Chọn ảnh") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImageOptions = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Từ thư viện")
                    }

                    TextButton(
                        onClick = {
                            showImageOptions = false
                            // Note: Camera requires more setup (file provider, permissions)
                            // For now, just use gallery
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Chụp ảnh")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean,
    onDelete: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }  // 🔥 THÊM
    val context = LocalContext.current  // 🔥 THÊM

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    showOptionsDialog = true  // 🔥 THAY ĐỔI
                }
            ),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            color = if (isCurrentUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Hiển thị ảnh nếu có
                message.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Ảnh",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (message.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Hiển thị text
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = if (isCurrentUser) Color.White else Color.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = formatFullTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser)
                        Color.White.copy(alpha = 0.7f)
                    else
                        Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }

    // 🔥 THÊM: Dialog với nhiều options
    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text("Tùy chọn") },
            text = {
                Column {
                    // Copy text
                    if (message.text.isNotBlank()) {
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("message", message.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Đã copy tin nhắn", Toast.LENGTH_SHORT).show()
                                showOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📋 Copy text")
                        }
                    }

                    // Delete (chỉ với tin nhắn của mình)
                    if (isCurrentUser) {
                        TextButton(
                            onClick = {
                                showOptionsDialog = false
                                showDeleteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🗑️ Xóa tin nhắn", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOptionsDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // 🔥 Dialog xác nhận xóa
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa tin nhắn") },
            text = { Text("Bạn có chắc muốn xóa tin nhắn này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}
