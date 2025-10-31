package com.example.freshcookapp.domain.model

data class Category(
    val id: Int,                 // Mã định danh thể loại
    val name: String,            // Tên thể loại (key: "thit", "banh", ...)
    val displayName: String,     // Tên hiển thị (ví dụ: "Thịt", "Bánh")
    val imageRes: Int,           // Ảnh đại diện cho thể loại
    val recipes: List<Recipe>    // Danh sách món thuộc thể loại này
)