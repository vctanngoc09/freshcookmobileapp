package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.RecentViewedEntity
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import com.example.freshcookapp.util.TextUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

class RecipeRepository(private val db: AppDatabase) {

    private val firestore = FirebaseFirestore.getInstance()
    // per-recipe mutexes to avoid concurrent toggles racing and causing inconsistent UI state
    private val toggleMutexes = ConcurrentHashMap<String, Mutex>()

    private fun mutexFor(recipeId: String): Mutex {
        return toggleMutexes.computeIfAbsent(recipeId) { Mutex() }
    }

    // --- CÁC HÀM LẤY DATA TRANG HOME ---
    fun getNewDishes() = db.recipeDao().getNewDishes()

    fun getRecipesByCategory(categoryId: String) =
        db.recipeDao().getRecipesByCategory(categoryId)


    // --- CÁC HÀM MỚI THÊM (CHO FAVORITE & DETAIL) ---

    // 1. Lấy chi tiết một món ăn bằng IDz
    suspend fun getRecipeById(id: String): RecipeEntity? {
        return db.recipeDao().getRecipeById(id)
    }

    suspend fun getRecipesByIds(ids: List<String>): List<RecipeEntity> {
        return db.recipeDao().getRecipesByIds(ids)
    }

    // Live flow for a single recipe
    fun getRecipeFlow(id: String) = db.recipeDao().getRecipeByIdFlow(id)


    // 2. Lấy danh sách các món đã thả tim
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>> {
        return db.recipeDao().getFavoriteRecipes()
    }

    // 3. Cập nhật trạng thái yêu thích
    // If likeCount is provided, update both is_favorite and like_count locally (used for optimistic updates);
    // otherwise only update the favorite flag.
    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean, likeCount: Int? = null) {
        if (likeCount != null) {
            db.recipeDao().updateFavoriteLocal(recipeId, isFavorite, likeCount)
        } else {
            db.recipeDao().updateFavoriteStatus(recipeId, isFavorite)
        }
    }

    // ⭐ Cập nhật nhanh trong Room để UI đổi ngay
    suspend fun updateFavoriteLocal(
        recipeId: String,
        isFavorite: Boolean,
        likeCount: Int
    ) {
        db.recipeDao().updateFavoriteLocal(
            recipeId = recipeId,
            isFavorite = isFavorite,
            likeCount = likeCount
        )
    }

    // New: perform Firestore transaction centrally and return success/failure
    suspend fun setFavoriteRemote(userId: String, recipeId: String, desiredState: Boolean): Boolean =
        suspendCancellableCoroutine { cont ->
            try {
                val recipeRef = firestore.collection("recipes").document(recipeId)
                val userFavRef = firestore.collection("users").document(userId).collection("favorites").document(recipeId)

                firestore.runTransaction { tx ->
                    val snap = tx.get(userFavRef)
                    val exists = snap.exists()

                    if (desiredState) {
                        if (!exists) {
                            tx.set(userFavRef, mapOf("addedAt" to FieldValue.serverTimestamp(), "recipeId" to recipeId))
                            tx.update(recipeRef, "likeCount", FieldValue.increment(1))
                        }
                    } else {
                        if (exists) {
                            tx.delete(userFavRef)
                            tx.update(recipeRef, "likeCount", FieldValue.increment(-1))
                        }
                    }
                    null
                }
                    .addOnSuccessListener {
                        if (!cont.isCompleted) cont.resume(true)
                    }
                    .addOnFailureListener {
                        if (!cont.isCompleted) cont.resume(false)
                    }
            } catch (_: Exception) {
                if (!cont.isCompleted) cont.resume(false)
            }
        }

    /*
     Atomic optimistic toggle:
      - acquires a per-recipe mutex
      - reads current local entity
      - applies optimistic local update (is_favorite + likeCount)
      - calls remote transaction (setFavoriteRemote)
      - rolls back local update if remote fails
    */
    suspend fun toggleFavoriteWithRemote(userId: String, recipeId: String, desiredState: Boolean) {
        val mutex = mutexFor(recipeId)
        mutex.withLock {
            val current = db.recipeDao().getRecipeById(recipeId) ?: return
            val currentCount = current.likeCount
            val newCount = if (desiredState) currentCount + 1 else max(0, currentCount - 1)

            // optimistic local update so UI reflects immediately
            db.recipeDao().updateFavoriteLocal(recipeId, desiredState, newCount)

            // perform remote; rollback if fails
            val success = try {
                setFavoriteRemote(userId, recipeId, desiredState)
            } catch (_: Exception) {
                false
            }

            if (!success) {
                // rollback to previous state
                db.recipeDao().updateFavoriteLocal(recipeId, !desiredState, currentCount)
            }
        }
    }

    // Gọi hàm này khi vào xem chi tiết (Lịch sử)
    suspend fun addToHistory(recipeId: String) {
        db.recipeDao().updateLastViewed(recipeId, System.currentTimeMillis())
    }

    // Gọi hàm này ở trang Xem gần đây

    suspend fun insertRecipe(recipe: RecipeEntity) {
        db.recipeDao().insert(recipe)
    }

    suspend fun insertRecipes(recipes: List<RecipeEntity>) {
        db.recipeDao().insertAll(recipes)
    }

    // ------------------- RECENTLY VIEWED (NEW) -------------------

    // ------------------- RECENTLY VIEWED (NEW) -------------------
    suspend fun addToRecentlyViewed(recipeId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1) Xóa bản cũ (nếu có) của cùng user + recipe
        db.recentViewedDao().remove(recipeId, uid)

        // 2) Thêm bản mới với timestamp mới nhất
        val item = RecentViewedEntity(
            recipeId = recipeId,
            userId = uid,
            timestamp = System.currentTimeMillis()
        )
        db.recentViewedDao().insert(item)
    }

    data class ViewedWithTime(
        val recentId: Int,            // ID từ bảng recent_viewed
        val recipe: RecipeEntity,
        val timestamp: Long
    )



    fun getRecentlyViewed(): Flow<List<ViewedWithTime>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        return db.recentViewedDao().getRecent(uid).map { list ->
            list.map { recent ->
                ViewedWithTime(
                    recentId = recent.id,              // ⭐ id thật từ Room recent_viewed
                    recipe = db.recipeDao().getRecipeById(recent.recipeId)!!,
                    timestamp = recent.timestamp
                )
            }
        }
    }


    suspend fun removeFromRecentlyViewed(recipeId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.recentViewedDao().remove(recipeId, uid)
    }


    // Xóa 1 món khỏi lịch sử
    suspend fun removeFromHistory(recipeId: String) {
        db.recipeDao().removeFromHistory(recipeId)
    }

    suspend fun removeFromRecentlyViewedByRecentId(recentId: Int) {
        db.recentViewedDao().removeByRecentId(recentId)
    }


    // --- SỬA LỖI Ở ĐÂY ---
    // Thêm cặp ngoặc () sau recipeDao
    fun getRelatedRecipes(categoryId: String, currentId: String): Flow<List<RecipeEntity>> {
        return db.recipeDao().getRelatedRecipes(categoryId, currentId)
    }

    fun getTrendingRecipes() = db.recipeDao().getTrendingRecipes()

    fun searchRecipes(keyword: String): Flow<List<RecipeEntity>> {
        return db.recipeDao().searchRecipes(keyword)
    }

    fun searchRecipesByName(keyword: String): List<RecipeEntity> {
        return db.recipeDao().searchByName(keyword)
    }

    fun searchLocal(keyword: String): Flow<List<RecipeEntity>> {
        val norm = TextUtils.normalizeKey(keyword)

        return db.recipeDao().getAllRecipes().map { list ->
            list.filter { recipe ->
                recipe.searchTokens.any { token ->
                    token.contains(norm)
                }
            }
        }
    }

    fun getAllRecipes(): Flow<List<RecipeEntity>> {
        return db.recipeDao().getAllRecipes()
    }




    // --- HÀM TẠO MÓN ---
    suspend fun saveRecipe(
        name: String,
        description: String,
        timeCook: Int?,
        people: Int?,
        imageUrl: String?,
        userId: String,
        categoryId: String,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>
    ) {
        val ingredientsList = ingredients.map { "${it.quantity} ${it.unit} ${it.name}" }
        val stepsList = instructions.map { "Bước ${it.stepNumber}: ${it.description}" }

        val recipeEntity = RecipeEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            timeCook = timeCook ?: 0,
            imageUrl = imageUrl,
            userId = userId,
            categoryId = categoryId,
            ingredients = ingredientsList,
            steps = stepsList,
            createdAt = System.currentTimeMillis()
        )
        db.recipeDao().insert(recipeEntity)
    }

    // --- HÀM MỚI CHO LOGOUT ---
    suspend fun clearLocalUserData() {
        db.recipeDao().clearAllFavorites()
        db.recipeDao().clearHistory()
    }

    // ===== PHÂN TRANG CHO CATEGORY / TRENDING / NEW =====

    suspend fun getTrendingPage(limit: Int, offset: Int) =
        db.recipeDao().getTrendingRecipesPage(limit, offset)

    suspend fun getNewDishesPage(limit: Int, offset: Int) =
        db.recipeDao().getNewDishesPage(limit, offset)

    suspend fun getRecipesByCategoryPage(
        categoryId: String,
        limit: Int,
        offset: Int
    ) = db.recipeDao().getRecipesByCategoryPage(categoryId, limit, offset)
}