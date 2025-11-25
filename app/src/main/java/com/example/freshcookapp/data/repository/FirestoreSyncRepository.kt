package com.example.freshcookapp.data.repository

import android.util.Log
import com.example.freshcookapp.data.local.dao.CategoryDao
import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.entity.CategoryEntity
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.firestore.Query

class FirestoreSyncRepository(
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao
) {
    private val firestore = Firebase.firestore

    suspend fun syncRecipes() {
        try {
            // 1. Lấy toàn bộ món ăn từ bảng "recipes"
            val snapshot = firestore.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Sắp xếp ngày tạo giảm dần (mới nhất lên đầu)
                .limit(100) // Chỉ lấy đúng 100 cái
                .get()
                .await()

            val recipeList = mutableListOf<RecipeEntity>()

            // Map để lưu danh mục tự động tìm thấy: (Mã danh mục -> Link ảnh đại diện)
            val foundCategoriesMap = mutableMapOf<String, String>()

            for (doc in snapshot.documents) {
                try {
                    // --- LẤY THÔNG TIN CƠ BẢN ---
                    val name = doc.getString("name") ?: "Món chưa đặt tên"
                    val time = doc.getLong("timeCook")?.toInt() ?: 15
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val description = doc.getString("description") ?: ""
                    val userId = doc.getString("userId") ?: "admin"

                    // Lấy categoryId (ví dụ: "noodle", "soup")
                    val catId = doc.getString("categoryId") ?: "other"

                    // 🔥 LOGIC: Tự động lấy ảnh món ăn làm ảnh danh mục
                    if (catId.isNotEmpty()) {
                        // Nếu danh mục này chưa có trong Map, hoặc chưa có ảnh
                        if (!foundCategoriesMap.containsKey(catId) || foundCategoriesMap[catId].isNullOrEmpty()) {
                            if (imageUrl.isNotEmpty()) {
                                foundCategoriesMap[catId] = imageUrl
                            } else {
                                // Nếu chưa có ảnh thì tạm lưu key, giá trị rỗng
                                if (!foundCategoriesMap.containsKey(catId)) {
                                    foundCategoriesMap[catId] = ""
                                }
                            }
                        }
                    }

                    val difficultyRaw = doc.getString("difficulty") ?: "medium"
                    val level = when (difficultyRaw.lowercase()) {
                        "easy" -> "Dễ"
                        "medium" -> "Trung bình"
                        "hard" -> "Khó"
                        else -> "Trung bình"
                    }

                    val createdAtString = doc.getString("createdAt")
                    val createdAt = parseDateToLong(createdAtString)

                    // --- LẤY SUB-COLLECTION: NGUYÊN LIỆU ---
                    val ingSnapshot = doc.reference.collection("recipeIngredients").get().await()
                    val ingredientsList = ingSnapshot.documents.map { ingDoc ->
                        val iName = ingDoc.getString("name") ?: ""
                        val iQty = ingDoc.getString("quantity") ?: ""
                        val iUnit = ingDoc.getString("unit") ?: ""
                        "$iQty $iUnit $iName".trim()
                    }

                    // --- LẤY SUB-COLLECTION: CÁCH LÀM ---
                    val stepSnapshot = doc.reference.collection("instruction")
                        .orderBy("step")
                        .get().await()

                    val stepsList = stepSnapshot.documents.map { stepDoc ->
                        val sStep = stepDoc.getLong("step") ?: 0
                        val sDesc = stepDoc.getString("description") ?: ""
                        "Bước $sStep: $sDesc"
                    }

                    // Tạo Entity Món Ăn
                    val entity = RecipeEntity(
                        id = doc.id,
                        name = name,
                        description = description,
                        timeCookMinutes = time,
                        imageUrl = imageUrl,
                        level = level,
                        ingredients = ingredientsList,
                        steps = stepsList,
                        userId = userId,
                        categoryId = catId,
                        createdAt = createdAt
                    )
                    recipeList.add(entity)

                } catch (e: Exception) {
                    Log.e("SyncError", "Lỗi đọc món: ${doc.id}", e)
                }
            }

            // 2. LƯU MÓN ĂN VÀO ROOM
            if (recipeList.isNotEmpty()) {
                recipeDao.refreshRecipes(recipeList)
                Log.d("FirestoreSync", "Đã tải xong ${recipeList.size} món ăn")
            }

            // 3. TẠO VÀ LƯU DANH MỤC (ĐÃ XỬ LÝ XÓA CŨ)
            val categoryEntities = foundCategoriesMap.map { (catKey, imgUrl) ->
                CategoryEntity(
                    id = catKey,                        // String OK
                    name = capitalizeFirstLetter(catKey),
                    imageUrl = imgUrl
                )
            }


            if (categoryEntities.isNotEmpty()) {
                // 🔥Xóa sạch danh mục cũ (rác) trước khi lưu cái mới
                categoryDao.deleteAll()

                categoryDao.insertAll(categoryEntities)
                Log.d("FirestoreSync", "Đã cập nhật ${categoryEntities.size} danh mục (Đã xóa rác cũ)")
            }

        } catch (e: Exception) {
            Log.e("FirestoreSync", "Lỗi đồng bộ tổng", e)
        }
    }

    private fun parseDateToLong(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return System.currentTimeMillis()
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun capitalizeFirstLetter(input: String): String {
        return if (input.isNotEmpty()) {
            input.substring(0, 1).uppercase(Locale.getDefault()) + input.substring(1)
        } else {
            input
        }
    }
}