package com.example.freshcookapp.domain.model

import com.google.firebase.firestore.PropertyName

data class Recipe(
    val id: String,

    // Dùng đúng field UI
    val name: String,
    val imageUrl: String? = null,
    val timeCook: Int = 0,
    val difficulty: String? = "Trung bình",

    val description: String = "",

    val isFavorite: Boolean = false,
    val likeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val people: Int = 1,

    // 🔥 SỬA LỖI: Thêm `userId` để ánh xạ trực tiếp từ Firestore
    @get:PropertyName("userId")
    val userId: String? = null,
    
    val author: Author,
    val authorName: String = "",
    val authorAvatar: String = "",
    val hashtags: List<String> = listOf(),
    @get:PropertyName("recipeIngredients")
    val ingredients: List<String> = listOf(),
    val instructions: List<InstructionStep> = listOf(),
    val relatedRecipes: List<RecipePreview> = listOf(),

    // Normalized tokens used for searching/filtering (lowercase, no diacritics)
    val searchTokens: List<String> = listOf()
)