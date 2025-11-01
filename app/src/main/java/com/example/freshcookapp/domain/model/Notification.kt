package com.example.freshcookapp.domain.model

data class Notification(
    val id: String,
    val user: User,
    val message: String,
    val time: String,
    val type: NotificationType
)

enum class NotificationType {
    LIKE,
    FOLLOW,
    COMMENT
}
