package com.example.freshcookapp.ui.screen.newcook

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await


class NewCookViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    /**
     * Hàm public duy nhất UI gọi khi bấm "Lên sóng"
     */
    fun saveRecipe(
        name: String,
        description: String,
        timeCookMinutes: Int?,               // phút
        people: Int?,                        // số người ăn
        imageUri: Uri?,                      // ảnh đại diện món ăn (có thể null)
        hashtags: List<String>,              // list hashtag người dùng nhập
        difficultyUi: String,                // "Dễ" / "Trung" / "Khó"
        ingredients: List<Ingredient>,       // list nguyên liệu
        instructions: List<Instruction>,     // list bước làm
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Lấy userId hiện tại (fallback "admin")
                val currentUserId =
                    FirebaseAuth.getInstance().currentUser?.uid ?: "admin"

                // 2. Tạo id cho recipe (dùng chung cho Firestore)
                val recipeId = UUID.randomUUID().toString()

                // 3. Upload ảnh đại diện (nếu có) lên Firebase Storage
                val imageUrl = uploadRecipeImageIfNeeded(recipeId, imageUri)

                // 4. Map difficulty từ UI -> Firestore
                val difficulty = when (difficultyUi) {
                    "Dễ" -> "easy"
                    "Trung" -> "medium"
                    "Khó" -> "hard"
                    else -> "medium"
                }

                // 5. CategoryId: tạm thời fix "soup" giống mẫu bạn đưa
                val categoryId = "soup"

                // 6. Lưu local Room (cho offline / home list)
                recipeRepository.saveRecipe(
                    name = name,
                    description = description,
                    timeCookMinutes = timeCookMinutes,
                    people = people,
                    imageUrl = imageUrl,
                    userId = currentUserId,
                    categoryId = categoryId,
                    ingredients = ingredients,
                    instructions = instructions
                )

                // 7. Lưu lên Firestore đúng cấu trúc
                saveRecipeToFirestore(
                    recipeId = recipeId,
                    name = name,
                    description = description,
                    timeCookMinutes = timeCookMinutes,
                    people = people,
                    imageUrl = imageUrl,
                    userId = currentUserId,
                    categoryId = categoryId,
                    hashtags = hashtags,
                    difficulty = difficulty,
                    ingredients = ingredients,
                    instructions = instructions
                )

                onSuccess()
            } catch (e: Throwable) {
                Log.e("NewCookViewModel", "Lỗi lưu món", e)
                onError(e)
            }
        }
    }

    /**
     * Upload ảnh đại diện (nếu có) lên Storage:
     * path: recpies_img/{recipeId}.jpg
     * Trả về downloadUrl hoặc "" nếu không có ảnh.
     */
    private suspend fun uploadRecipeImageIfNeeded(
        recipeId: String,
        imageUri: Uri?
    ): String {
        if (imageUri == null) return ""

        return try {
            val storage = FirebaseStorage.getInstance()

            // 📌 Upload vào folder riêng của món
            val ref = storage.reference
                .child("recpies_img/$recipeId/main.jpg")

            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()

        } catch (e: Exception) {
            Log.e("NewCookViewModel", "Upload ảnh đại diện thất bại", e)
            ""
        }
    }

    //    hàm thêm ảnh từng bước của món ăn vào đúng chuẩn như này recpies_img/{recipeId}/steps/step_{index}.jpg
    private suspend fun uploadStepImage(
        recipeId: String,
        stepIndex: Int,
        imageUri: String?
    ): String {
        if (imageUri.isNullOrEmpty()) return ""

        return try {
            val uri = Uri.parse(imageUri)
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
     * Lưu recipe vào Firestore với:
     *  - Document chính: recipes/{recipeId}
     *  - Subcollection: recipeIngredients
     *  - Subcollection: instruction
     */
    private suspend fun saveRecipeToFirestore(
        recipeId: String,
        name: String,
        description: String,
        timeCookMinutes: Int?,
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
        val recipeRef = db.collection("recipes").document(recipeId)

        val safeTime = (timeCookMinutes ?: 0)
        val safePeople = (people ?: 1)

        // "2025-11-15T00:00:00" format
        val createdAt = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        ).format(java.util.Date())

        // === Document chính ===
        val recipeData = hashMapOf(
            "id" to recipeId,
            "name" to name,
            "description" to description,
            "categoryId" to categoryId,
            "createdAt" to createdAt,
            "difficulty" to difficulty,
            "hashtagId" to hashtags,
            "imageUrl" to imageUrl,
            "likeCount" to 0,           // default 0
            "people" to safePeople,
            "timeCook" to safeTime,
            "userId" to userId
        )

        // 1. Lưu document chính
        recipeRef.set(recipeData).await()

        // 2. Subcollection: recipeIngredients
        val ingredientsCol = recipeRef.collection("recipeIngredients")
        ingredients.forEach { ing ->
            val ingData = hashMapOf(
                "name" to ing.name,
                "quantity" to ing.quantity,
                "unit" to ing.unit,
                "note" to ing.notes
            )
            ingredientsCol.add(ingData).await()
        }

        // 3. Subcollection: instruction
        // --- Save steps + upload images ---
        val instructionCol = recipeRef.collection("instruction")

        instructions.forEachIndexed { index, ins ->

            val uploadedStepImageUrl = uploadStepImage(recipeId, index, ins.imageUrl)

            val insData = hashMapOf(
                "step" to ins.stepNumber,
                "description" to ins.description,
                "imageUrl" to uploadedStepImageUrl   // ảnh từ Storage
            )

            instructionCol.add(insData).await()
        }

        Log.d("NewCookViewModel", "Đã lưu Firestore với ID: $recipeId")
    }
}