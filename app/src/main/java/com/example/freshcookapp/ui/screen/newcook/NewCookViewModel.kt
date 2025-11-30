package com.example.freshcookapp.ui.screen.newcook

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.Normalizer


class NewCookViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    val isUploading = mutableStateOf(false)

    private fun normalizeText(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    /**
     * Hàm public duy nhất UI gọi khi bấm "Lên sóng"
     */
    fun saveRecipe(
        name: String,
        description: String,
        timeCook: Int?,               // phút
        people: Int?,                        // số người ăn
        imageUri: Uri?,                      // ảnh đại diện món ăn (có thể null)
        hashtags: List<String>,              // list hashtag người dùng nhập
        difficultyUi: String,                // "Dễ" / "Trung" / "Khó"
        categoryId: String?,
        ingredients: List<Ingredient>,       // list nguyên liệu
        instructions: List<Instruction>,     // list bước làm
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isUploading.value = true
                val currentUserId =
                    FirebaseAuth.getInstance().currentUser?.uid ?: "admin"

                // 1. TẠO ID DUY NHẤT VÀ DÙNG NÓ CHO CẢ DỰ ÁN (Storage/Room/Firestore)
                val recipeId = FirebaseFirestore.getInstance().collection("recipes").document().id


                // 3. Upload ảnh đại diện (nếu có) lên Firebase Storage
                val imageUrl = uploadRecipeImageIfNeeded(recipeId, imageUri)

                // 4. Map difficulty từ UI -> Firestore
                val difficulty = when (difficultyUi) {
                    "Dễ" -> "easy"
                    "Trung" -> "medium"
                    "Khó" -> "hard"
                    else -> "medium"
                }

                // 5. CategoryId
                val finalCategoryId = categoryId ?: "other"

                // 7. Lưu lên Firestore đúng cấu trúc
                saveRecipeToFirestore(
                    recipeId = recipeId, // Dùng ID đã tạo
                    name = name, description = description, timeCook = timeCook,
                    people = people, imageUrl = imageUrl, userId = currentUserId,
                    categoryId = finalCategoryId, hashtags = hashtags, difficulty = difficulty,
                    ingredients = ingredients, instructions = instructions
                )

                isUploading.value = false   // 🔥 DONE
                onSuccess()
            } catch (e: Throwable) {
                isUploading.value = false
                Log.e("NewCookViewModel", "Lỗi lưu món", e)
                onError(e)
            }
        }
    }

    /**
     * Upload ảnh đại diện (nếu có) lên Storage:
     */
    private suspend fun uploadRecipeImageIfNeeded(
        recipeId: String,
        imageUri: Uri?
    ): String {
        if (imageUri == null) return ""

        return try {
            val storage = FirebaseStorage.getInstance()

            val ref = storage.reference
                .child("recpies_img/$recipeId/main.jpg")

            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()

        } catch (e: Exception) {
            Log.e("NewCookViewModel", "Upload ảnh đại diện thất bại", e)
            ""
        }
    }

    //    hàm thêm ảnh từng bước của món ăn vào đúng chuẩn
    private suspend fun uploadStepImage(
        recipeId: String,
        stepIndex: Int,
        imageUri: String?
    ): String {
        if (imageUri.isNullOrEmpty()) return ""

        return try {
            val uri = imageUri.toUri()
            val storage = FirebaseStorage.getInstance()

            val ref = storage.reference
                .child("recpies_img/$recipeId/steps/step_${stepIndex + 1}.jpg")

            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()

        } catch (e: Exception) {
            Log.e("NewCookViewModel", "Upload step image failed", e)
            ""
        }
    }


    /**
     * Lưu recipe vào Firestore
     */
    private suspend fun saveRecipeToFirestore(
        recipeId: String,
        name: String,
        description: String,
        timeCook: Int?,
        people: Int?,
        imageUrl: String,
        userId: String,
        categoryId: String,
        hashtags: List<String>,
        difficulty: String,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>
    ) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        val recipeDoc = db.collection("recipes").document(recipeId)

        // ====== 1️⃣ LƯU DOCUMENT CHÍNH — NHANH NHẤT ======
        val safeTime = timeCook ?: 0
        val safePeople = people ?: 1

        val createdAt = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        ).format(java.util.Date())

//        val searchTokens =
//            (listOf(name) + ingredients.map { it.name })
//                .map { normalizeText(it) }
//                .flatMap { it.split(" ") }
//                .filter { it.isNotBlank() }
//                .distinct()
        val normName = normalizeText(name)
        val nameParts = normName.split(" ").filter { it.isNotBlank() }

        // Token 1 từ
        val singleTokens = nameParts

        // Token 2 từ (“bun bo”, “bo hue”)
        val pairTokens = nameParts.windowed(size = 2, step = 1)
            .map { it.joinToString(" ") }

        // Token full cụm (“bun bo hue”)
        val fullToken = listOf(normName)

        // Token liền không dấu (“bunbohue”)
        val compactToken = listOf(normName.replace(" ", ""))

        // Token nguyên liệu như cũ
        val ingTokens = ingredients
            .map { normalizeText(it.name) }
            .flatMap { it.split(" ") }
            .filter { it.isNotBlank() }

        // Gom tất cả
        val searchTokens = (singleTokens + pairTokens + fullToken + compactToken + ingTokens)
            .distinct()


        val recipeData = hashMapOf(
            "id" to recipeId,
            "name" to name,
            "description" to description,
            "categoryId" to categoryId,
            "createdAt" to createdAt,
            "difficulty" to difficulty,
            "hashtagId" to hashtags,
            "imageUrl" to imageUrl,
            "likeCount" to 0,
            "people" to safePeople,
            "timeCook" to safeTime,
            "userId" to userId,
            "searchTokens" to searchTokens
        )

        recipeDoc.set(recipeData).await()


        // ====== 2️⃣ UPLOAD ẢNH STEP + LƯU INSTRUCTION SONG SONG ======

        val instructionCol = recipeDoc.collection("instruction")

        val instructionTasks = instructions.mapIndexed { index, step ->

            viewModelScope.async {
                // Upload ảnh step (nếu có)
                val uploadedUrl =
                    if (step.imageUrl.isNullOrBlank()) ""
                    else uploadStepImage(recipeId, index, step.imageUrl)

                // Build data
                val stepData = hashMapOf(
                    "step" to step.stepNumber,
                    "description" to step.description,
                    "imageUrl" to uploadedUrl
                )

                instructionCol.add(stepData).await()
            }
        }

        // CHẠY TẤT CẢ CÙNG LÚC
        instructionTasks.awaitAll()


        // ====== 3️⃣ LƯU INGREDIENT SONG SONG ======

        val ingCol = recipeDoc.collection("recipeIngredients")

        val ingredientTasks = ingredients.map { ing ->
            viewModelScope.async {
                val data = hashMapOf(
                    "name" to ing.name,
                    "quantity" to ing.quantity,
                    "unit" to ing.unit,
                    "note" to ing.notes
                )
                ingCol.add(data).await()
            }
        }

        ingredientTasks.awaitAll()

        Log.d("NewCookViewModel", "🔥 Tối ưu: Lưu Firestore nhanh hoàn tất cho ID: $recipeId")
    }
}