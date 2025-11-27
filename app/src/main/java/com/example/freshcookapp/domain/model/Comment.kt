package com.example.freshcookapp.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val id: String = "",
    val userId: String = "",
    val recipeId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val text: String = "",
    @ServerTimestamp
    var timestamp: Date? = null,
    val likedBy: List<String> = emptyList()
)
