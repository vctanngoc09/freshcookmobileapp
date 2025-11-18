package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.dao.SearchDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

class SearchRepository(
    private val recipeDao: RecipeDao,
    private val searchDao: SearchDao
) {

    // Gợi ý (autocomplete) theo tên món
    fun suggestNames(keyword: String): Flow<List<String>> {
        return searchDao.suggestRecipeNames(keyword)
    }

    // Kết quả tìm kiếm: danh sách món đầy đủ
    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchRecipes(keyword)
    }
}
