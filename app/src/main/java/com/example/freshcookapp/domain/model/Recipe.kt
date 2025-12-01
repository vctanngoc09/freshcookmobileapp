package com.example.freshcookapp.domain.model

import com.google.firebase.firestore.PropertyName

data class Recipe(
    val id: String,

    val name: String,
    val imageUrl: String? = null,

    // 🔥 THÊM DÒNG NÀY: Link video hướng dẫn
    val videoUrl: String? = null,

    val timeCook: Int = 0,
    val difficulty: String? = "Trung bình",

    val description: String = "",

    val isFavorite: Boolean = false,
    val likeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val people: Int = 1,

    @get:PropertyName("userId")
    val userId: String? = null,

    val author: Author,
    // Những trường này nếu đã có object Author thì hơi thừa, nhưng giữ nguyên để tránh lỗi code cũ
    val authorName: String = "",
    val authorAvatar: String = "",

    val hashtags: List<String> = listOf(),

    @get:PropertyName("recipeIngredients")
    val ingredients: List<String> = listOf(),

    val instructions: List<InstructionStep> = listOf(),
    val relatedRecipes: List<RecipePreview> = listOf(),

    val searchTokens: List<String> = listOf()
)