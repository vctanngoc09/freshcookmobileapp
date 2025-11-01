package com.example.freshcookapp.domain.model

import com.example.freshcookapp.R

object DemoData {

    // --- ĐỊNH NGHĨA TÁC GIẢ ---
    val authorTanNgoc = Author("author1", "Bởi Cua Tan Ngoc", R.drawable.avatar1.toString(), "@ngocvctn09")
    val authorHoangAnh = Author("author2", "Bởi Hoàng Anh", R.drawable.avatar1.toString(), "@hoanganh")

    // --- TẠO DỮ LIỆU GỐC (CHI TIẾT) ---
    val recipeRauMuong = Recipe(
        id = "r1",
        title = "Rau muống ngâm chua ngọt",
        imageRes = R.drawable.img_food1,
        time = "30 min",
        level = "Dễ",
        isFavorite = true, // Sẽ hiện ở Yêu thích
        author = authorTanNgoc,
        hashtags = listOf("#raumong", "#ngamchua"),
        ingredients = listOf("Rau muống", "Tỏi, Ớt", "Nước mắm, Đường, Giấm"),
        instructions = listOf(
            InstructionStep(1, "Rau muống nhặt, rửa sạch, luộc sơ và ngâm nước đá."),
            InstructionStep(2, "Pha nước mắm, đường, giấm, tỏi, ớt rồi cho rau vào ngâm.")
        ),
        relatedRecipes = listOf(
            RecipePreview("r2", "Cà pháo muối xổi", R.drawable.img_food2)
        )
    )

    val recipeCaPhao = Recipe(
        id = "r2",
        title = "Cà pháo muối xổi",
        imageRes = R.drawable.img_food2,
        time = "45 min",
        level = "Trung bình",
        isFavorite = true, // Sẽ hiện ở Yêu thích
        author = authorHoangAnh,
        hashtags = listOf("#caphao", "#muoi"),
        ingredients = listOf("Cà pháo", "Riềng, Tỏi, Ớt", "Nước mắm, Đường, Chanh"),
        instructions = listOf(
            InstructionStep(1, "Cà pháo bổ đôi, ngâm nước muối loãng."),
            InstructionStep(2, "Giã riềng, tỏi, ớt. Pha nước mắm chua ngọt rồi trộn đều với cà pháo.")
        )
    )

    val recipePancake = Recipe(
        id = "r3",
        title = "Honey pancakes with...",
        imageRes = R.drawable.img_food1,
        time = "30 min",
        level = "Dễ",
        isFavorite = false,
        author = authorHoangAnh,
        hashtags = listOf("#pancake")
    )

    val recipeSpaghetti = Recipe(
        id = "r4",
        title = "Spaghetti carbonara",
        imageRes = R.drawable.img_food2,
        time = "25 min",
        level = "Trung bình",
        isFavorite = true,
        author = authorTanNgoc
    )

    val recipeComChien = Recipe(
        id = "r5",
        title = "Cơm chiên hải sản",
        imageRes = R.drawable.img_food1,
        time = "20 min",
        level = "Dễ",
        isFavorite = false,
        author = authorHoangAnh
    )

    val recipeSandwich = Recipe(
        id = "r6",
        title = "Sandwich with chicken and onion",
        imageRes = R.drawable.img_food1,
        time = "30p trước",
        level = "Dễ",
        isFavorite = false,
        author = authorTanNgoc
    )

    val recipeSalmon = Recipe(
        id = "r7",
        title = "Grilled salmon with herbs",
        imageRes = R.drawable.img_food2,
        time = "1h trước",
        level = "Trung bình",
        isFavorite = false, // Sửa: Gốc là false
        author = authorHoangAnh
    )

    val recipePasta = Recipe(
        id = "r8",
        title = "Pasta with creamy sauce",
        imageRes = R.drawable.img_food1,
        time = "2h trước",
        level = "Khó",
        isFavorite = false,
        author = authorHoangAnh
    )

    val recipeThitKho = Recipe(
        id = "r9",
        title = "Thịt kho tàu",
        imageRes = R.drawable.img_food1,
        time = "30 min",
        level = "Dễ",
        isFavorite = false,
        author = authorTanNgoc
    )

    val recipeThitXao = Recipe(
        id = "r10",
        title = "Thịt xào hành",
        imageRes = R.drawable.img_food2,
        time = "20 min",
        level = "Dễ",
        isFavorite = true,
        author = authorHoangAnh
    )

    val recipeFlan = Recipe(
        id = "r11",
        title = "Bánh flan caramel",
        imageRes = R.drawable.img_food1,
        time = "40 min",
        level = "Trung bình",
        isFavorite = false,
        author = authorTanNgoc
    )

    val recipeBongLan = Recipe(
        id = "r12",
        title = "Bánh bông lan",
        imageRes = R.drawable.img_food2,
        time = "45 min",
        level = "Trung bình",
        isFavorite = false,
        author = authorHoangAnh
    )

    val recipeCanhBi = Recipe(
        id = "r13",
        title = "Canh bí đỏ tôm khô",
        imageRes = R.drawable.img_food1,
        time = "25 min",
        level = "Dễ",
        isFavorite = false,
        author = authorTanNgoc
    )

    val recipeCanhGa = Recipe(
        id = "r14",
        title = "Canh gà nấm",
        imageRes = R.drawable.img_food2,
        time = "30 min",
        level = "Trung bình",
        isFavorite = false,
        author = authorHoangAnh
    )

    // --- TẠO DANH SÁCH TỔNG HỢP ---
    val allRecipes = listOf(
        recipeRauMuong, recipeCaPhao, recipePancake, recipeSpaghetti, recipeComChien,
        recipeSandwich, recipeSalmon, recipePasta, recipeThitKho, recipeThitXao,
        recipeFlan, recipeBongLan, recipeCanhBi, recipeCanhGa
    ).distinctBy { it.id }

    // --- DỮ LIỆU CHO Home.kt ---
    // (Giống hệt file Home.kt gốc của bạn)
    val trendingRecipes = listOf(recipePancake, recipeSpaghetti, recipeComChien)
    val recommendedRecipes = listOf(recipeSandwich, recipeSalmon, recipePasta)
    val meatRecipes = listOf(recipeThitKho, recipeThitXao)
    val cakeRecipes = listOf(recipeFlan, recipeBongLan)
    val soupRecipes = listOf(recipeCanhBi, recipeCanhGa)

    val trendingCategories = listOf(
        Category(1, "thit", "Thịt", R.drawable.kw_thit, meatRecipes),
        Category(2, "banh", "Bánh", R.drawable.kw_banh, cakeRecipes),
        Category(3, "thucdon", "Thực đơn mỗi ngày", R.drawable.kw_thit, listOf()),
        Category(4, "thitkho", "Thịt kho", R.drawable.kw_banh, meatRecipes),
        Category(5, "namduiga", "Nấm đùi gà", R.drawable.kw_thit, soupRecipes),
        Category(6, "goi", "Gỏi", R.drawable.kw_banh, listOf())
    )

    val newDishes = listOf(
        Triple(R.drawable.img_food1, "Thịt gà xào măng", "Trần Thị Tuyết T."),
        Triple(R.drawable.img_food2, "Lẩu cháo chim bồ câu", "Bòn Bon"),
        Triple(R.drawable.img_food1, "Bánh xếp", "Huyen le Tran"),
        Triple(R.drawable.img_food2, "Bánh flan caramel", "Ngọc Mai"),
        Triple(R.drawable.img_food1, "Cơm chiên hải sản", "Hoàng Anh")
    )

    // --- DỮ LIỆU CHO Favorite.kt ---
    val favoriteRecipes: List<Recipe> = allRecipes.filter { it.isFavorite }

    // --- HÀM TÌM KIẾM (CHO Detail) ---
    fun findRecipeById(id: String?): Recipe? {
        if (id == null) return null
        return allRecipes.find { it.id == id }
    }
}