package com.example.freshcookapp.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val photoUrl: String? = null,
    val bio: String = ""
)
