package com.example.freshcookapp.data.model

data class Chat(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participants: Map<String, Map<String, Any>> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val typing: Map<String, Boolean> = emptyMap()
)

