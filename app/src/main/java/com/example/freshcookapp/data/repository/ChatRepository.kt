package com.example.freshcookapp.data.repository

import android.util.Log
import com.example.freshcookapp.data.model.Chat
import com.example.freshcookapp.data.model.ChatMessage
import com.example.freshcookapp.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Lấy danh sách chat của user hiện tại
    fun getChatsFlow(): Flow<List<Chat>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listener = db.collection("chats")
            .whereArrayContains("participantIds", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Error getting chats", error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Chat::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error parsing chat", e)
                        null
                    }
                } ?: emptyList()

                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    // Lấy tin nhắn của một chat
    fun getMessagesFlow(chatId: String, limit: Int = 50): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)  // Đổi sang DESC để lấy mới nhất
            .limit(limit.toLong())  // 🔥 THÊM LIMIT
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Error getting messages", error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error parsing message", e)
                        null
                    }
                } ?: emptyList()

                // Reverse lại để hiển thị đúng thứ tự (cũ → mới)
                trySend(messages.reversed())
            }

        awaitClose { listener.remove() }
    }

    // Gửi tin nhắn
    suspend fun sendMessage(
        chatId: String,
        text: String,
        imageUrl: String? = null
    ): Result<Unit> = try {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        
        val message = ChatMessage(
            senderId = currentUserId,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        // Thêm message vào subcollection
        val messageRef = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .await()

        // Cập nhật lastMessage và lastMessageTime trong chat document
        db.collection("chats")
            .document(chatId)
            .update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to System.currentTimeMillis()
                )
            )
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error sending message", e)
        Result.failure(e)
    }

    // Tạo chat mới hoặc lấy chat đã có
    suspend fun createOrGetChat(
        otherUserId: String,
        otherUserName: String,
        otherUserPhoto: String?
    ): Result<String> = try {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val currentUser = auth.currentUser
        
        // Kiểm tra xem chat đã tồn tại chưa
        val existingChats = db.collection("chats")
            .whereArrayContains("participantIds", currentUserId)
            .get()
            .await()

        val existingChat = existingChats.documents.firstOrNull { doc ->
            val participantIds = doc.get("participantIds") as? List<*>
            participantIds?.contains(otherUserId) == true
        }

        if (existingChat != null) {
            Result.success(existingChat.id)
        } else {
            // Tạo chat mới
            val chat = Chat(
                participantIds = listOf(currentUserId, otherUserId),
                participants = mapOf(
                    currentUserId to mapOf(
                        "id" to currentUserId,
                        "username" to (currentUser?.displayName ?: "Unknown"),
                        "photoUrl" to (currentUser?.photoUrl?.toString() ?: "")
                    ),
                    otherUserId to mapOf(
                        "id" to otherUserId,
                        "username" to otherUserName,
                        "photoUrl" to (otherUserPhoto ?: "")
                    )
                ),
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis()
            )

            val docRef = db.collection("chats").add(chat).await()
            Result.success(docRef.id)
        }
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error creating chat", e)
        Result.failure(e)
    }

    // Tìm kiếm users để chat
    suspend fun searchUsers(query: String): Result<List<User>> = try {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        
        val snapshot = db.collection("users")
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(20)
            .get()
            .await()

        val users = snapshot.documents.mapNotNull { doc ->
            try {
                val user = doc.toObject(User::class.java)?.copy(id = doc.id)
                // Không hiển thị chính mình trong kết quả tìm kiếm
                if (user?.id != currentUserId) user else null
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error parsing user", e)
                null
            }
        }

        Result.success(users)
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error searching users", e)
        Result.failure(e)
    }

    // Đánh dấu user đang typing
    suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> = try {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        
        db.collection("chats")
            .document(chatId)
            .update("typing.$currentUserId", isTyping)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error setting typing status", e)
        Result.failure(e)
    }

    // Lắng nghe trạng thái typing của user khác
    fun getTypingStatusFlow(chatId: String, otherUserId: String): Flow<Boolean> = callbackFlow {
        val listener = db.collection("chats")
            .document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Error getting typing status", error)
                    return@addSnapshotListener
                }

                val typingMap = snapshot?.get("typing") as? Map<*, *>
                val isTyping = typingMap?.get(otherUserId) as? Boolean ?: false
                trySend(isTyping)
            }

        awaitClose { listener.remove() }
    }

    // 🔥 THÊM MỚI: Load thêm tin nhắn cũ hơn (khi scroll lên)
    suspend fun loadMoreMessages(
        chatId: String,
        beforeTimestamp: Long,
        limit: Int = 50
    ): Result<List<ChatMessage>> = try {
        val snapshot = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .whereLessThan("timestamp", beforeTimestamp)
            .limit(limit.toLong())
            .get()
            .await()

        val messages = snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error parsing message", e)
                null
            }
        }

        Result.success(messages.reversed())
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error loading more messages", e)
        Result.failure(e)
    }

    // 🔥 THÊM MỚI: Upload ảnh lên Firebase Storage
    suspend fun uploadImage(imageUri: android.net.Uri): Result<String> = try {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val storage = com.google.firebase.storage.FirebaseStorage.getInstance()

        // Tạo unique filename
        val timestamp = System.currentTimeMillis()
        val filename = "chat_images/${currentUserId}_${timestamp}.jpg"
        val storageRef = storage.reference.child(filename)

        // Upload file
        val uploadTask = storageRef.putFile(imageUri).await()

        // Lấy download URL
        val downloadUrl = storageRef.downloadUrl.await().toString()

        Result.success(downloadUrl)
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error uploading image", e)
        Result.failure(e)
    }

    // 🔥 THÊM MỚI: Xóa tin nhắn
    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> = try {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")

        // Lấy message để check ownership
        val messageDoc = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .get()
            .await()

        val senderId = messageDoc.getString("senderId")

        // Chỉ cho phép xóa tin nhắn của mình
        if (senderId != currentUserId) {
            throw Exception("Bạn chỉ có thể xóa tin nhắn của mình")
        }

        // Xóa message
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("ChatRepository", "Error deleting message", e)
        Result.failure(e)
    }
}
