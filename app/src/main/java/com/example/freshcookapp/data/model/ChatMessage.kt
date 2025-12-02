 package com.example.freshcookapp.data.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)

