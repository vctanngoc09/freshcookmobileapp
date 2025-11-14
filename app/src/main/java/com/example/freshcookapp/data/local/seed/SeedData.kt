package com.example.freshcookapp.data.local.seed

import com.example.freshcookapp.data.local.entity.CategoryEntity
import com.example.freshcookapp.data.local.entity.NewDishEntity
import com.example.freshcookapp.data.local.entity.RecipeEntity

object SeedData {
    val categories = listOf(
        CategoryEntity(
            id = 1, name = "Thịt", key = "thit",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthit.jpg?alt=media&token=c893ea96-9bf6-4b21-b4e9-b108526e3d83"
        ),
        CategoryEntity(
            id = 2, name = "Bánh", key = "banh",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fbanh.jpg?alt=media&token=e89357a7-2eee-49ac-9685-7794faf2d088"
        ),
        CategoryEntity(
            id = 3, name = "Thực đơn mỗi ngày", key = "thucdon",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthuc_don_moi_ngay.jpeg?alt=media&token=4bffe0a2-5070-4ba4-a0a9-d8d289152396"
        ),
        CategoryEntity(
            id = 4, name = "Thịt kho", key = "thitkho",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthit_kho.png?alt=media&token=f54d053f-ad21-4932-96df-2f6f01246e2b"
        ),
        CategoryEntity(
            id = 5, name = "Nấm đùi gà", key = "namduiga",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fnam.jpg?alt=media&token=0f982992-376c-4a5e-aff5-1398ad563901"
        ),
        CategoryEntity(
            id = 6, name = "Gỏi", key = "goi",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fgoi.jpg?alt=media&token=d8389fc0-3e81-4127-896a-e7d3a5064a9e"
        )
    )

    val newDishes = listOf(
        NewDishEntity(
            title = "Thịt gà xào măng", authorName = "Trần Thị Tuyết T.",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthit_ga_xao_mang.jpg?alt=media&token=d60761af-554d-426d-a1db-b5f0081f134b"
        ),
        NewDishEntity(
            title = "Lẩu cháo chim bồ câu", authorName = "Bòn Bon",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Flau_chao_chim_bo_cau.jpg?alt=media&token=1f3ea11c-3866-46b6-b76d-cc73aea2d2a6"
        ),
        NewDishEntity(
            title = "Bánh xếp", authorName = "Huyen le Tran",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fbanh_xep.jpg?alt=media&token=c7a85adb-198e-43e8-bdf9-1be468637f38"
        ),
        NewDishEntity(
            title = "Bánh flan caramel", authorName = "Ngọc Mai",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fbanh_plan_caramel.jpg?alt=media&token=53385c2b-6d24-4b42-94e3-266f35bc4d40"
        ),
        NewDishEntity(
            title = "Cơm chiên hải sản", authorName = "Hoàng Anh",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fcom_chien_tr%E1%BB%A9ng.jpg?alt=media&token=b61a3ccb-69d2-4ee9-92b3-dec62f53f447"
        )
    )

    val recipes = listOf(

        RecipeEntity(
            name = "Gà chiên giòn",
            description = "Gà chiên giòn kiểu Hàn Quốc",
            timeCookMinutes = 25, people = 2,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fga_chien_gion.jpg?alt=media&token=f9103bd5-a568-404f-b95e-3d90702ab704",
            userId = 1, categoryId = 1
        ),
        RecipeEntity(
            name = "Cơm chiên trứng",
            description = "Cơm chiên đơn giản dễ làm",
            timeCookMinutes = 10, people = 1,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fcom_chien_tr%E1%BB%A9ng.jpg?alt=media&token=b61a3ccb-69d2-4ee9-92b3-dec62f53f447",
            userId = 1, categoryId = 3
        ),

        RecipeEntity(
            name = "Rau muống ngâm chua ngọt",
            description = "Rau muống giòn giòn, chua ngọt, ăn kèm cơm rất ngon.",
            timeCookMinutes = 30, people = 4,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fgoi.jpg?alt=media&token=d8389fc0-3e81-4127-896a-e7d3a5064a9e", // Tạm
            userId = 1, categoryId = 6
        ),

        RecipeEntity(
            name = "Cà pháo muối xổi",
            description = "Cà pháo muối xổi giòn tan, vị chua cay mặn ngọt hài hòa.",
            timeCookMinutes = 45, people = 4,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fgoi.jpg?alt=media&token=d8389fc0-3e81-4127-896a-e7d3a5064a9e", // Tạm
            userId = 1, categoryId = 6
        ),

        RecipeEntity(
            name = "Honey pancakes ",
            description = "Bánh pancake mật ong cho bữa sáng.",
            timeCookMinutes = 30, people = 2,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fbanh.jpg?alt=media&token=e89357a7-2eee-49ac-9685-7794faf2d088", // Tạm
            userId = 1, categoryId = 2
        ),

        RecipeEntity(
            name = "Spaghetti carbonara",
            description = "Mỳ Ý sốt carbonara béo ngậy.",
            timeCookMinutes = 25, people = 2,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthuc_don_moi_ngay.jpeg?alt=media&token=4bffe0a2-5070-4ba4-a0a9-d8d289152396", // Tạm
            userId = 1, categoryId = 3
        ),

        RecipeEntity(
            name = "Cơm chiên hải sản",
            description = "Cơm chiên hải sản nhanh gọn, đủ chất.",
            timeCookMinutes = 20, people = 2,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthuc_don_moi_ngay.jpeg?alt=media&token=4bffe0a2-5070-4ba4-a0a9-d8d289152396", // Tạm
            userId = 1, categoryId = 3
        ),

        RecipeEntity(
            name = "Sandwich with chicken and onion",
            description = "Sandwich gà và hành tây cho bữa sáng.",
            timeCookMinutes = 30, people = 1,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthit.jpg?alt=media&token=c893ea96-9bf6-4b21-b4e9-b108526e3d83", // Tạm
            userId = 1, categoryId = 3
        ),

        RecipeEntity(
            name = "Grilled salmon with herbs",
            description = "Cá hồi nướng thảo mộc tốt cho sức khỏe.",
            timeCookMinutes = 60, people = 2,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthuc_don_moi_ngay.jpeg?alt=media&token=4bffe0a2-5070-4ba4-a0a9-d8d289152396", // Tạm
            userId = 1, categoryId = 3
        ),

        RecipeEntity(
            name = "Pasta with creamy sauce",
            description = "Mỳ Ý sốt kem béo ngậy, nhanh gọn.",
            timeCookMinutes = 120, people = 2,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthuc_don_moi_ngay.jpeg?alt=media&token=4bffe0a2-5070-4ba4-a0a9-d8d289152396", // Tạm
            userId = 1, categoryId = 3
        ),

        RecipeEntity(
            name = "Thịt kho tàu",
            description = "Món thịt kho tàu truyền thống, đậm đà.",
            timeCookMinutes = 30, people = 4,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthit_kho.png?alt=media&token=f54d053f-ad21-4932-96df-2f6f01246e2b", // Tạm
            userId = 1, categoryId = 4
        ),

        RecipeEntity(
            name = "Thịt xào hành",
            description = "Thịt xào hành đơn giản, nhanh gọn.",
            timeCookMinutes = 20, people = 3,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fthit.jpg?alt=media&token=c893ea96-9bf6-4b21-b4e9-b108526e3d83", // Tạm
            userId = 1, categoryId = 1
        ),

        RecipeEntity(
            name = "Bánh flan caramel",
            description = "Bánh flan mềm mịn, béo ngậy.",
            timeCookMinutes = 40, people = 4,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fbanh.jpg?alt=media&token=e89357a7-2eee-49ac-9685-7794faf2d088", // Tạm
            userId = 1, categoryId = 2
        ),

        RecipeEntity(
            name = "Bánh bông lan",
            description = "Bánh bông lan trứng muối.",
            timeCookMinutes = 45, people = 4,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fbanh.jpg?alt=media&token=e89357a7-2eee-49ac-9685-7794faf2d088", // Tạm
            userId = 1, categoryId = 2
        ),

        RecipeEntity(
            name = "Canh bí đỏ tôm khô",
            description = "Canh bí đỏ ngọt thanh, bổ dưỡng.",
            timeCookMinutes = 25, people = 3,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fnam.jpg?alt=media&token=0f982992-376c-4a5e-aff5-1398ad563901", // Tạm
            userId = 1, categoryId = 5
        ),

        RecipeEntity(
            name = "Canh gà nấm",
            description = "Canh gà nấu nấm thanh đạm.",
            timeCookMinutes = 30, people = 3,
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/freshcookapp-b376c.firebasestorage.app/o/recipe_images%2Fnam.jpg?alt=media&token=0f982992-376c-4a5e-aff5-1398ad563901", // Tạm
            userId = 1, categoryId = 5
        )
    )
}