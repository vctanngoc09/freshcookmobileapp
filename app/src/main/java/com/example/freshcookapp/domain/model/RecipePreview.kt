package com.example.freshcookapp.domain.model

data class RecipePreview(
    val id: String,
    val title: String,
    val author: String = "", // Thêm trường tác giả
    val time: String = "",   // Thêm trường thời gian
    val imageUrl: String? = null // Đổi từ Int sang String? để chứa link ảnh
)