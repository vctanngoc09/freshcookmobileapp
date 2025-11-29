package com.example.freshcookapp.domain.model

data class RecipePreview(
    val id: String,
    val title: String,
    val author: String = "",
    val time: String = "",
    val imageUrl: String? = null,
    // --- THÊM DÒNG NÀY ---
    val isFavorite: Boolean = false
)