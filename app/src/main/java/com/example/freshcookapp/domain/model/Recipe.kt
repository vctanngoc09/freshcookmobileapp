package com.example.freshcookapp.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val time: String,
    val level: String,
    val description: String = "",

    // --- QUAN TRỌNG: TRẠNG THÁI VÀ SỐ LƯỢNG ---
    val isFavorite: Boolean = false, // Tôi có thích không? (Để tô đỏ tim)
    val likeCount: Int = 0,          // Có bao nhiêu người thích? (Để hiện số)

    val author: Author,
    val hashtags: List<String> = listOf(),
    val ingredients: List<String> = listOf(),
    val instructions: List<InstructionStep> = listOf(),
    val relatedRecipes: List<RecipePreview> = listOf(),

    // Normalized tokens used for searching/filtering (lowercase, no diacritics)
    val searchTokens: List<String> = listOf()
)