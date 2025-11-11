package com.example.freshcookapp.domain.model

data class Author(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val username: String = ""
)