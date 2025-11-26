package com.example.freshcookapp.data.sync

import android.util.Log
import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreHomeSync(
    private val recipeDao: RecipeDao
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     *  Sync dành riêng cho HOME
     *  Không lấy ingredients / steps / user info → load cực nhanh
     */
    fun start() {
        firestore.collection("recipes")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.e("HomeSync", "Lỗi lắng nghe HomeSync", e)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) return@addSnapshotListener

                scope.launch {

                    val list = mutableListOf<RecipeEntity>()

                    for (doc in snapshot.documents) {
                        try {

                            // ----------- LẤY FIELD NHẸ CHO HOME -------------
                            val id = doc.id
                            val name = doc.getString("name") ?: ""
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            val timeCook = doc.getLong("timeCook")?.toInt() ?: 0
                            val difficultyRaw = doc.getString("difficulty") ?: "medium"
                            val categoryId = doc.getString("categoryId") ?: "other"
                            val createdAtString = doc.getString("createdAt") ?: ""
                            val like = doc.getLong("likeCount")?.toInt() ?: 0
                            val people = doc.getLong("people")?.toInt() ?: 1

                            // Mapping difficulty
                            val difficulty = when (difficultyRaw.lowercase()) {
                                "easy" -> "Dễ"
                                "medium" -> "Trung bình"
                                "hard" -> "Khó"
                                else -> "Trung bình"
                            }

                            // Parse createdAt string → long
                            val createdAt = try {
                                utilsParseDate(createdAtString)
                            } catch (_: Exception) {
                                System.currentTimeMillis()
                            }

                            // ----------- GIỮ LẠI LIKE + FAVORITE LOCAL -------------
                            val local = recipeDao.getRecipeById(id)

                            val finalIsFavorite = local?.isFavorite ?: false
                            val finalLikeCount = local?.likeCount ?: like

                            // ---------- ENTITY NHẸ DÀNH RIÊNG CHO HOME -----------
                            val entity = RecipeEntity(
                                id = id,
                                name = name,
                                description = local?.description ?: "",
                                timeCook = timeCook,
                                imageUrl = imageUrl,
                                difficulty = difficulty,
                                ingredients = local?.ingredients ?: emptyList(),
                                steps = local?.steps ?: emptyList(),
                                people = people,
                                userId = local?.userId ?: "",
                                categoryId = categoryId,
                                createdAt = createdAt,
                                authorName = local?.authorName ?: "",
                                authorAvatar = local?.authorAvatar ?: "",
                                isFavorite = finalIsFavorite,
                                likeCount = finalLikeCount
                            )

                            list.add(entity)

                        } catch (err: Exception) {
                            Log.e("HomeSync", "Lỗi parse document HomeSync", err)
                        }
                    }

                    // ------------ LƯU NHẸ VÀO ROOM ------------------
                    if (list.isNotEmpty()) {
                        recipeDao.insertAll(list)
                        Log.d("HomeSync", "Đã đồng bộ ${list.size} món (FAST HOME MODE).")
                    }
                }
            }
    }

    // Parse createdAt string to Long
    private fun utilsParseDate(dateString: String?): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}