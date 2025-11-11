package com.example.freshcookapp.data.local.seed

import com.example.freshcookapp.data.local.entity.RecipeEntity

object SeedData {
    val recipes = listOf(
        RecipeEntity(
            name = "Gà chiên giòn",
            description = "Gà chiên giòn kiểu Hàn Quốc",
            timeCookMinutes = 25,
            people = 2,
            imageUrl = "https://i.imgur.com/6uEwZtF.jpeg",
            userId = 1,
            categoryId = 1
        ),
        RecipeEntity(
            name = "Cơm chiên trứng",
            description = "Cơm chiên đơn giản dễ làm",
            timeCookMinutes = 10,
            people = 1,
            imageUrl = "https://i.imgur.com/ZlE2q7r.jpeg",
            userId = 1,
            categoryId = 2
        )
    )
}