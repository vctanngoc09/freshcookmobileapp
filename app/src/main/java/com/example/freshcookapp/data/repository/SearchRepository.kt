package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.dao.SearchHistoryDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.local.entity.SearchHistoryEntity
import com.example.freshcookapp.util.TextUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchRepository(
    private val recipeDao: RecipeDao,
    private val historyDao: SearchHistoryDao
) {

    fun suggestNames(keyword: String): Flow<List<String>> {
        return recipeDao.searchRecipes(keyword).map { list ->
            val seen = linkedSetOf<String>()
            val out = mutableListOf<String>()
            for (recipe in list) {
                val name = recipe.name.trim()
                val key = TextUtils.normalizeKey(name)
                if (seen.add(key)) {
                    out.add(name)
                }
            }
            out
        }
    }

    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchRecipes(keyword)
    }

    fun getSearchHistory(): Flow<List<String>> {
        return historyDao.getSearchHistory()
    }

    fun getAllHistory(): Flow<List<SearchHistoryEntity>> {
        return historyDao.getAllHistory()
    }

    suspend fun saveSearchQuery(query: String) {
        if (query.isNotBlank()) {
            val entity = SearchHistoryEntity(
                query = query.trim(),
                timestamp = System.currentTimeMillis()
            )
            historyDao.insert(entity)
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        historyDao.deleteByQuery(query)
    }

    suspend fun clearAllHistory() {
        historyDao.clearAll()
    }
}
