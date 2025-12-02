package com.example.freshcookapp.data.sync

import android.util.Log
import com.example.freshcookapp.data.local.dao.RecipeDao
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.QuerySnapshot

class FirestoreHomeSync(
    private val recipeDao: RecipeDao
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     *  Hàm mới: Lấy dữ liệu một lần, dành cho Pull-to-refresh.
     *  Sử dụng .get() thay vì .addSnapshotListener()
     */
    suspend fun forceRefresh() {
        try {
            val snapshot = firestore.collection("recipes")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await() // Sử dụng .get().await() để lấy dữ liệu một lần

            processSnapshot(snapshot) // Tách logic xử lý ra hàm riêng
        } catch (e: FirebaseFirestoreException) {
            // Xử lý lỗi Firestore cụ thể
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.e("HomeSync", """
                        ❌ PERMISSION_DENIED: Không có quyền đọc dữ liệu recipes
                        
                        🔧 Cách sửa:
                        1. Vào Firebase Console: https://console.firebase.google.com/
                        2. Chọn project: freshcookapp-b376c
                        3. Vào Firestore Database → Rules
                        4. Thay rules bằng: allow read, write: if request.auth != null;
                        5. Click Publish và chờ 5 giây
                        
                        📖 Chi tiết: Xem file FIRESTORE_RULES_FIX.md
                    """.trimIndent(), e)
                }
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    Log.e("HomeSync", "❌ UNAVAILABLE: Không thể kết nối Firestore. Kiểm tra internet!", e)
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Log.e("HomeSync", "❌ UNAUTHENTICATED: Chưa đăng nhập. Vui lòng đăng nhập lại!", e)
                }
                else -> {
                    Log.e("HomeSync", "❌ Lỗi Firestore: ${e.code} - ${e.message}", e)
                }
            }
            throw e
        } catch (e: Exception) {
            Log.e("HomeSync", "Lỗi khi forceRefresh", e)
            // Ném lại lỗi để coroutine ở Home.kt có thể bắt được
            throw e
        }
    }

    /**
     *  Hàm cũ: Thiết lập listener thời gian thực.
     *  Vẫn hữu ích khi ứng dụng khởi động lần đầu.
     */
    fun startListener() {
        firestore.collection("recipes")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.e("HomeSync", "Lỗi lắng nghe HomeSync", e)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) return@addSnapshotListener

                scope.launch {
                    processSnapshot(snapshot)
                }
            }
    }

    /**
     *  Hàm chung để xử lý dữ liệu từ Firestore và cập nhật vào Room.
     */
    private suspend fun processSnapshot(snapshot: QuerySnapshot) {
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
                // Giữ lại số like local nếu nó lớn hơn, phòng trường hợp optimistic update chưa kịp đồng bộ
                val finalLikeCount = maxOf(local?.likeCount ?: 0, like)

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
