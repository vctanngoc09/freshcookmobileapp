package com.example.freshcookapp.domain.model

data class Recipe(
    val imageRes: Int,
    val title: String,
    val time: String,
    val level: String,
    val isFavorite: Boolean
)
