package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.dao.RecipeIndexDao
import com.example.freshcookapp.data.local.dao.SearchDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

class SearchRepository(
    private val indexDao: RecipeIndexDao
) {

    // Gợi ý theo tên món
    fun suggestNames(keyword: String): Flow<List<String>> =
        indexDao.suggest(keyword)

    // Lấy id theo tên → dùng để fetch Firestore detail
    suspend fun getRecipeId(name: String): String =
        indexDao.getIdByName(name)
}

