package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.dao.SearchHistoryDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.data.local.entity.SearchHistoryEntity
import com.example.freshcookapp.util.TextUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchRepository(
    private val recipeDao: RecipeDao,
    private val historyDao: SearchHistoryDao
) {

    fun suggestNames(keyword: String): Flow<List<String>> {
        val norm = TextUtils.normalizeKey(keyword)

        return recipeDao.getAllRecipes().map { list ->
            list
                .filter { recipe ->
                    recipe.searchTokens.any { it.contains(norm) }
                }
                .map { it.name.trim() }
                .distinct()
                .take(10)
        }
    }

    fun searchLocal(keyword: String): Flow<List<RecipeEntity>> {
        val norm = TextUtils.normalizeKey(keyword)

        return recipeDao.getAllRecipes().map { list ->
            list.filter { recipe ->
                recipe.searchTokens.any { token ->
                    token.contains(norm)
                }
            }
        }
    }

    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchRecipes(keyword)
    }

    // ⭐⭐ SỬA ĐÚNG CHUẨN — Lấy lịch sử của user hiện tại
    fun getSearchHistory(): Flow<List<String>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return historyDao.getSearchHistory(uid)
    }

    fun getAllHistory(): Flow<List<SearchHistoryEntity>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return historyDao.getAllHistory(uid)
    }

    suspend fun saveSearchQuery(query: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (query.isNotBlank()) {
            val entity = SearchHistoryEntity(
                query = query.trim(),
                timestamp = System.currentTimeMillis(),
                userId = uid
            )
            historyDao.insert(entity)
        }
    }

    // ⭐⭐ Sửa để chỉ xóa query của user hiện tại
    suspend fun deleteSearchQuery(query: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        historyDao.deleteByQuery(query, uid)
    }

    // ⭐⭐ Sửa để chỉ xóa lịch sử của user hiện tại
    suspend fun clearAllHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        historyDao.clearAll(uid)
    }
}