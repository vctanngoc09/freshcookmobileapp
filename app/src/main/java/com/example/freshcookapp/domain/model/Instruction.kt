package com.example.freshcookapp.domain.model

data class Instruction(
    var stepNumber: Int = 1,
    var description: String = "",
    var imageUrl: String = ""
)
