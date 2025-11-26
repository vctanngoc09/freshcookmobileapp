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
    private val historyDao: SearchHistoryDao // <--- THÊM THAM SỐ NÀY
) {

    // --- PHẦN 1: TÌM KIẾM MÓN ĂN (GIỮ NGUYÊN) ---

    // Trả về danh sách tên món ăn (dùng cho gợi ý khi đang gõ)
    fun suggestNames(keyword: String): Flow<List<String>> {
        return recipeDao.searchRecipes(keyword).map { list ->
            // Nếu DB chứa nhiều món cùng tên (hoặc khác dấu/Viết hoa), loại bỏ trùng
            // Nhưng giữ nguyên thứ tự xuất hiện (first occurrence wins)
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

    // Trả về danh sách Entity món ăn (dùng cho trang kết quả)
    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchRecipes(keyword)
    }

    // --- PHẦN 2: LỊCH SỬ TÌM KIẾM (MỚI THÊM) ---

    // Lấy danh sách từ khóa lịch sử (đã được sắp xếp mới nhất ở Dao)
    fun getSearchHistory(): Flow<List<String>> {
        return historyDao.getSearchHistory()
    }

    // Lưu từ khóa vào lịch sử
    suspend fun saveSearchQuery(query: String) {
        // Chỉ lưu nếu từ khóa không rỗng
        if (query.isNotBlank()) {
            val entity = SearchHistoryEntity(
                query = query.trim(),
                timestamp = System.currentTimeMillis()
            )
            historyDao.insert(entity)
        }
    }

    // Xóa toàn bộ lịch sử (nếu cần dùng nút "Xóa tất cả")
    suspend fun clearHistory() {
        historyDao.clearAll()
    }
}