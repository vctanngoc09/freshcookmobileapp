package com.example.freshcookapp.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val time: String,
    val level: String,

    // --- THÊM DÒNG NÀY ĐỂ KHỚP VỚI VIEWMODEL ---
    val description: String = "",

    val isFavorite: Boolean = false,
    val author: Author,
    val hashtags: List<String> = listOf(),
    val ingredients: List<String> = listOf(),
    val instructions: List<InstructionStep> = listOf(),
    val relatedRecipes: List<RecipePreview> = listOf()
)