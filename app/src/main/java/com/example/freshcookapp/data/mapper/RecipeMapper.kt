package com.example.freshcookapp.data.mapper

import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.InstructionStep

fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        id = this.id,
        title = this.name,
        imageUrl = this.imageUrl,
        imageRes = null,
        time = "${this.timeCookMinutes} phút",
        level = this.level ?: "Trung bình",
        ingredients = this.ingredients, // Đã format sẵn bên Sync: "500 g Cánh gà"
        instructions = this.steps.mapIndexed { index, content ->
            // Content đã có dạng "Bước 1: Ướp gà", ta chỉ cần hiển thị
            InstructionStep(
                stepNumber = index + 1,
                description = content.substringAfter(": ").trim(), // Lấy phần nội dung sau dấu :
                imageUrl = null
            )
        },
        isFavorite = false,
        author = Author("Admin", ""), // Có thể sửa sau nếu load User
        hashtags = emptyList(),
        relatedRecipes = emptyList()
    )
}