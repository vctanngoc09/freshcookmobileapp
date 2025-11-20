package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // <--- QUAN TRỌNG: Phải có import này

class SearchRepository(
    private val recipeDao: RecipeDao
) {
    // Hàm này trả về danh sách tên món ăn (String) để hiển thị gợi ý text
    fun suggestNames(keyword: String): Flow<List<String>> {
        // Lấy danh sách RecipeEntity từ Dao, sau đó map sang List<String>
        return recipeDao.searchRecipes(keyword).map { list ->
            list.map { recipe -> recipe.name } // Sửa 'it' thành 'recipe' cho rõ ràng
        }
    }

    // Hàm tìm kiếm thực tế (trả về Entity để ViewModel map sang RecipeCard)
    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchRecipes(keyword)
    }
}