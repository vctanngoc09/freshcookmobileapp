package com.example.freshcookapp.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val imageRes: Int,
    val time: String,
    val level: String,
    val isFavorite: Boolean = false,

    // Dữ liệu cho trang Detail
    val author: Author,
    val hashtags: List<String> = listOf(),
    val ingredients: List<String> = listOf(),
    val instructions: List<InstructionStep> = listOf(),
    val relatedRecipes: List<RecipePreview> = listOf()
)