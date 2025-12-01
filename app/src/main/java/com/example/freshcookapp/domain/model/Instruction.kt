package com.example.freshcookapp.domain.model

import android.net.Uri

data class Instruction(
    val stepNumber: Int,
    val description: String = "",
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(), // Thêm dòng này
    @Transient val imageUri: Uri? = null
)
