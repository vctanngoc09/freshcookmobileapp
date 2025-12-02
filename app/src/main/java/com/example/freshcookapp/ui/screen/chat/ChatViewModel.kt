package com.example.freshcookapp.ui.screen.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.model.Chat
import com.example.freshcookapp.data.model.ChatMessage
import com.example.freshcookapp.domain.model.User
import com.example.freshcookapp.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    // State cho danh sách chat
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    // State cho messages trong chat hiện tại
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // State cho chat hiện tại
    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat.asStateFlow()

    // State cho tìm kiếm users
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    // State cho trạng thái typing
    private val _isOtherUserTyping = MutableStateFlow(false)
    val isOtherUserTyping: StateFlow<Boolean> = _isOtherUserTyping.asStateFlow()

    // Loading và error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var chatsJob: Job? = null
    private var messagesJob: Job? = null
    private var typingJob: Job? = null

    init {
        loadChats()
    }

    // Lấy danh sách chat
    private fun loadChats() {
        chatsJob?.cancel()
        chatsJob = viewModelScope.launch {
            try {
                repository.getChatsFlow().collect { chatList ->
                    _chats.value = chatList
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tải danh sách chat: ${e.message}"
                Log.e("ChatViewModel", "Error loading chats", e)
            }
        }
    }

    // Load messages của một chat
    fun loadChatMessages(chatId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            try {
                _isLoading.value = true

                // Load chat info
                _chats.value.find { it.id == chatId }?.let {
                    _currentChat.value = it
                }

                // Load messages
                repository.getMessagesFlow(chatId).collect { messageList ->
                    _messages.value = messageList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tải tin nhắn: ${e.message}"
                _isLoading.value = false
                Log.e("ChatViewModel", "Error loading messages", e)
            }
        }

        // Listen to typing status
        listenToTypingStatus(chatId)
    }

    // Gửi tin nhắn
    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val result = repository.sendMessage(chatId, text)
                if (result.isFailure) {
                    _error.value = "Gửi tin nhắn thất bại"
                }
                // Stop typing indicator
                setTypingStatus(chatId, false)
            } catch (e: Exception) {
                _error.value = "Lỗi gửi tin nhắn: ${e.message}"
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    // Gửi ảnh
    fun sendImage(chatId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                val result = repository.sendMessage(chatId, "[Hình ảnh]", imageUrl)
                if (result.isFailure) {
                    _error.value = "Gửi ảnh thất bại"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi gửi ảnh: ${e.message}"
                Log.e("ChatViewModel", "Error sending image", e)
            }
        }
    }

    // Tạo hoặc lấy chat với user khác
    fun createChat(
        otherUserId: String,
        otherUserName: String,
        otherUserPhoto: String?,
        onChatCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.createOrGetChat(otherUserId, otherUserName, otherUserPhoto)

                result.onSuccess { chatId ->
                    onChatCreated(chatId)
                    _isLoading.value = false
                }

                result.onFailure { e ->
                    _error.value = "Lỗi tạo chat: ${e.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tạo chat: ${e.message}"
                _isLoading.value = false
                Log.e("ChatViewModel", "Error creating chat", e)
            }
        }
    }

    // Tìm kiếm users
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.searchUsers(query)

                result.onSuccess { users ->
                    _searchResults.value = users
                    _isLoading.value = false
                }

                result.onFailure { e ->
                    _error.value = "Lỗi tìm kiếm: ${e.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tìm kiếm: ${e.message}"
                _isLoading.value = false
                Log.e("ChatViewModel", "Error searching users", e)
            }
        }
    }

    // Set typing status
    fun setTypingStatus(chatId: String, isTyping: Boolean) {
        viewModelScope.launch {
            try {
                repository.setTypingStatus(chatId, isTyping)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error setting typing status", e)
            }
        }
    }

    // Listen to typing status
    private fun listenToTypingStatus(chatId: String) {
        val otherUserId = getOtherUserId(chatId) ?: return

        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            try {
                repository.getTypingStatusFlow(chatId, otherUserId).collect { isTyping ->
                    _isOtherUserTyping.value = isTyping
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error listening to typing status", e)
            }
        }
    }

    // Helper: Lấy user ID của người kia
    private fun getOtherUserId(chatId: String): String? {
        val currentUserId = auth.currentUser?.uid ?: return null
        val chat = _chats.value.find { it.id == chatId } ?: return null
        return chat.participantIds.firstOrNull { it != currentUserId }
    }

    // Helper: Lấy thông tin user kia
    fun getOtherUser(chat: Chat): Map<String, Any>? {
        val currentUserId = auth.currentUser?.uid ?: return null
        val otherUserId = chat.participantIds.firstOrNull { it != currentUserId } ?: return null
        return chat.participants[otherUserId]
    }

    // Helper: Lấy ảnh của user kia
    fun getOtherUserPhoto(chat: Chat): String? {
        val otherUser = getOtherUser(chat) ?: return null
        return otherUser["photoUrl"] as? String
    }

    // Helper: Check nếu user kia đang typing
    fun isOtherUserTyping(chatId: String): Boolean {
        return _isOtherUserTyping.value
    }

    // Helper: Xử lý khi user gõ text
    fun onTypingTextChanged(chatId: String, text: String) {
        setTypingStatus(chatId, text.isNotEmpty())
    }

    // Clear error
    fun clearError() {
        _error.value = null
    }

    // Get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun onCleared() {
        super.onCleared()
        chatsJob?.cancel()
        messagesJob?.cancel()
        typingJob?.cancel()
    }
}
