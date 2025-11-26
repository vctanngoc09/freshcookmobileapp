package com.example.freshcookapp.data.sync

import android.util.Log
import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class FirestoreRealtimeSync(
    private val recipeDao: RecipeDao //Dùng RecipeDao để lưu full thông tin
) {
    private val firestore = Firebase.firestore
    // Tạo Scope riêng để chạy các tác vụ ngầm khi có dữ liệu mới
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        // Lắng nghe collection "recipes" theo thời gian thực
        firestore.collection("recipes").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("RealtimeSync", "Lỗi lắng nghe Firestore", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                // Khi có thay đổi, ta mở Coroutine để xử lý việc lấy Sub-collections
                scope.launch {
                    val listEntities = mutableListOf<RecipeEntity>()

                    for (doc in snapshot.documents) {
                        try {
                            // 1. Lấy thông tin cơ bản (Khớp với cấu trúc Firestore của bạn)
                            val name = doc.getString("name") ?: "No Name"
                            val time = doc.getLong("timeCook")?.toInt() ?: 0
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            val description = doc.getString("description") ?: ""
                            val userId = doc.getString("userId") ?: "admin"
                            val catId = doc.getString("categoryId") ?: "general"

                            val difficultyRaw = doc.getString("difficulty") ?: "medium"
                            val difficulty = when (difficultyRaw.lowercase()) {
                                "easy" -> "Dễ"
                                "medium" -> "Trung bình"
                                "hard" -> "Khó"
                                else -> "Trung bình"
                            }

                            val createdAtString = doc.getString("createdAt")
                            val createdAt = parseDateToLong(createdAtString)

                            // 2. Lấy Sub-collection: recipeIngredients ( QUAN TRỌNG )
                            val ingSnapshot = doc.reference.collection("recipeIngredients").get().await()
                            val ingredientsList = ingSnapshot.documents.map {
                                val iQty = it.getString("quantity") ?: ""
                                val iUnit = it.getString("unit") ?: ""
                                val iName = it.getString("name") ?: ""
                                "$iQty $iUnit $iName".trim()
                            }

                            // 3. Lấy Sub-collection: instruction ( QUAN TRỌNG )
                            val stepSnapshot = doc.reference.collection("instruction").orderBy("step").get().await()
                            val stepsList = stepSnapshot.documents.map {
                                val sStep = it.getLong("step") ?: 0
                                val sDesc = it.getString("description") ?: ""
                                "Bước $sStep: $sDesc"
                            }

                            // 4. Tạo Entity đầy đủ
                            // Lấy thông tin tác giả từ users/{userId}
                            val userSnap = firestore.collection("users")
                                .document(userId)
                                .get()
                                .await()

                            val authorName = userSnap.getString("name") ?: "Ẩn danh"
                            val authorAvatar = userSnap.getString("photoUrl") ?: ""

                            // Lấy số phần ăn
                            val people = doc.getLong("people")?.toInt() ?: 1

                            // --- IMPORTANT: preserve local favorite/like values ---
                            // Nếu đã có bản ghi trong Room, giữ trạng thái isFavorite và likeCount của local
                            val existingLocal = try {
                                recipeDao.getRecipeById(doc.id)
                            } catch (_: Exception) {
                                null
                            }

                            // Prefer likeCount from Firestore if present, otherwise keep local, otherwise 0
                            val likeCountFromDoc = doc.getLong("likeCount")?.toInt()
                            val finalLikeCount = likeCountFromDoc ?: existingLocal?.likeCount ?: 0

                            val finalIsFavorite = existingLocal?.isFavorite ?: false

                            val entity = RecipeEntity(
                                id = doc.id,
                                name = name,
                                description = description,
                                timeCook = time,
                                imageUrl = imageUrl,
                                difficulty = difficulty,
                                ingredients = ingredientsList,
                                steps = stepsList,

                                // ⭐ giữ các field quan trọng
                                people = people,
                                userId = userId,
                                authorName = authorName,
                                authorAvatar = authorAvatar,

                                categoryId = catId,
                                createdAt = createdAt,

                                // preserve values
                                isFavorite = finalIsFavorite,
                                likeCount = finalLikeCount
                            )

                            listEntities.add(entity)

                        } catch (ex: Exception) {
                            Log.e("RealtimeSync", "Lỗi parse document: ${doc.id}", ex)
                        }
                    }

                    // 5. Cập nhật vào Room
                    if (listEntities.isNotEmpty()) {
                        recipeDao.insertAll(listEntities)
                        Log.d("RealtimeSync", "Đã cập nhật realtime ${listEntities.size} món ăn (kèm chi tiết).")
                    }
                }
            }
        }
    }

    private fun parseDateToLong(dateString: String?): Long {
        if (dateString.isNullOrEmpty()) return System.currentTimeMillis()
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}