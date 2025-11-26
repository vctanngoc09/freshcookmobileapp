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

            // 1️⃣ TẢI DANH MỤC TỪ FIRESTORE (CHỈ LÀM 1 LẦN)
            val categorySnapshot = firestore.collection("categories").get().await()

            val categoryEntities = categorySnapshot.documents.map { doc ->
                CategoryEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: "Danh mục",
                    imageUrl = doc.getString("imageUrl") ?: ""  // ảnh mặc định
                )
            }

            // Lưu danh mục vào Room
            categoryDao.deleteAll()
            categoryDao.insertAll(categoryEntities)



            // 2️⃣ TẢI RECIPES TỪ FIRESTORE
            val snapshot = firestore.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val recipeList = mutableListOf<RecipeEntity>()

            for (doc in snapshot.documents) {
                try {
                    val name = doc.getString("name") ?: "Món chưa đặt tên"
                    val time = doc.getLong("timeCook")?.toInt() ?: 15
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val description = doc.getString("description") ?: ""
                    val userId = doc.getString("userId") ?: "admin"
                    val catId = doc.getString("categoryId") ?: "other"

                    val difficultyRaw = doc.getString("difficulty") ?: "medium"
                    val difficulty = when (difficultyRaw.lowercase()) {
                        "easy" -> "Dễ"
                        "medium" -> "Trung bình"
                        "hard" -> "Khó"
                        else -> "Trung bình"
                    }

                    val createdAtString = doc.getString("createdAt")
                    val createdAt = parseDateToLong(createdAtString)
                    val people = doc.getLong("people")?.toInt() ?: 1



                    // Sub-collection ingredients
                    val ingSnapshot = doc.reference.collection("recipeIngredients").get().await()
                    val ingredientsList = ingSnapshot.documents.map {
                        val iName = it.getString("name") ?: ""
                        val iQty = it.getString("quantity") ?: ""
                        val iUnit = it.getString("unit") ?: ""
                        "$iQty $iUnit $iName".trim()
                    }

                    // Sub-collection instruction
                    val stepSnapshot = doc.reference.collection("instruction")
                        .orderBy("step")
                        .get().await()

                    val stepsList = stepSnapshot.documents.map {
                        val step = it.getLong("step") ?: 0
                        val desc = it.getString("description") ?: ""
                        "Bước $step: $desc"
                    }

                    // Lấy thông tin tác giả
                    val userSnap = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    val authorName = userSnap.getString("name") ?: "Người dùng"
                    val authorAvatar = userSnap.getString("photoUrl") ?: ""


                    val entity = RecipeEntity(
                        id = doc.id,
                        name = name,
                        description = description,
                        timeCook = time,
                        imageUrl = imageUrl,
                        difficulty = difficulty,
                        ingredients = ingredientsList,
                        steps = stepsList,
                        people = people,
                        userId = userId,
                        categoryId = catId,
                        createdAt = createdAt,
                        authorName = authorName,
                        authorAvatar = authorAvatar
                    )


                    recipeList.add(entity)

                } catch (e: Exception) {
                    Log.e("SyncError", "Lỗi đọc món: ${doc.id}", e)
                }
            }



            // 3️⃣ LƯU RECIPES
            if (recipeList.isNotEmpty()) {
                recipeDao.refreshRecipes(recipeList)
            }


            // 4️⃣ CẬP NHẬT ẢNH DANH MỤC (NẾU CHƯA CÓ)
            recipeList.forEach { recipe ->
                if (recipe.imageUrl?.isNotEmpty() == true) {
                    val category = categoryEntities.find { it.id == recipe.categoryId }
                    if (category != null && category.imageUrl.isNullOrEmpty()) {
                        categoryDao.updateImage(recipe.categoryId, recipe.imageUrl!!)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("FirestoreSync", "Lỗi đồng bộ tổng", e)
        }
    }


    private fun parseDateToLong(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
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