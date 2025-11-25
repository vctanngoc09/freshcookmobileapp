package com.example.freshcookapp.domain.model

data class InstructionStep(
    val stepNumber: Int,
    val description: String,
    val imageUrl: String? = null
)