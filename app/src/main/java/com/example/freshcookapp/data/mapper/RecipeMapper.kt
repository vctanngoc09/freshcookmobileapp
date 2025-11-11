package com.example.freshcookapp.data.mapper

import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.domain.model.Recipe
import com.example.freshcookapp.domain.model.Author

fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        id = this.id.toString(),
        title = this.name,
        imageRes = null,          // vì ta sẽ dùng imageUrl
        time = "${this.timeCookMinutes ?: 0} phút",
        level = "Dễ",             // tạm cứng, sau chỉnh sau
        isFavorite = false,
        author = Author("Ẩn danh", ""),
        hashtags = emptyList(),
        ingredients = emptyList(),
        instructions = emptyList(),
        relatedRecipes = emptyList(),
        imageUrl = this.imageUrl
    )
}