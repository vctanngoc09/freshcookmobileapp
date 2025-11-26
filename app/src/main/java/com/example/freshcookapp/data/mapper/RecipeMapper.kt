package com.example.freshcookapp.data.mapper

import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.InstructionStep

fun RecipeEntity.toRecipe(): Recipe {
    val authorObj = Author(
        id = this.userId,
        name = this.authorName,
        avatarUrl = this.authorAvatar
    )
    return Recipe(
        id = this.id,
        name = this.name,
        imageUrl = this.imageUrl,

        // UI dùng trực tiếp number → không string "45 phút"
        timeCook = this.timeCook,
        difficulty = this.difficulty ?: "Trung bình",

        description = this.description ?: "",
        people = this.people,

        isFavorite = this.isFavorite,
        likeCount = this.likeCount,

        author = authorObj,
        authorName = this.authorName,
        authorAvatar = this.authorAvatar,

        ingredients = this.ingredients,
        instructions = this.steps.mapIndexed { index, content ->
            InstructionStep(
                stepNumber = index + 1,
                description = content.substringAfter(": ").trim()
            )
        }
    )
}