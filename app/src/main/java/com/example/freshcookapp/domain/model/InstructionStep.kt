package com.example.freshcookapp.domain.model

data class InstructionStep(
    val stepNumber: Int,
    val description: String,

    // Ảnh chính (để tương thích ngược với dữ liệu cũ)
    val imageUrl: String? = null,

    // 🔥 THÊM DÒNG NÀY: Danh sách nhiều ảnh trong 1 bước
    val imageUrls: List<String> = emptyList()
)