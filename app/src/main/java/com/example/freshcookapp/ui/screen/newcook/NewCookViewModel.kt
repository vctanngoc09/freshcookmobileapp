package com.example.freshcookapp.ui.screen.newcook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NewCookViewModel(private val recipeRepository: RecipeRepository) : ViewModel() {

    fun saveRecipe(
        name: String,
        description: String,
        timeCookMinutes: Int?,
        people: Int?,
        imageUrl: String?,
        userId: Long, // Code cũ truyền vào Long, ta sẽ convert sang String bên dưới
        categoryId: Long, // Code cũ truyền vào Long, ta sẽ convert
        ingredients: List<com.example.freshcookapp.domain.model.Ingredient>,
        instructions: List<com.example.freshcookapp.domain.model.Instruction>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Chuẩn bị dữ liệu String
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
                val categoryString = "category_$categoryId" // Hoặc bạn map số sang chữ (vd: 1 -> "soup")

                // 2. Lưu vào Room (Local)
                // Lưu ý: Hàm saveRecipe trong Repo giờ không trả về ID nữa, nó tự sinh UUID
                recipeRepository.saveRecipe(
                    name = name,
                    description = description,
                    timeCookMinutes = timeCookMinutes,
                    people = people,
                    imageUrl = imageUrl,
                    userId = currentUserId, // Truyền String
                    categoryId = categoryString, // Truyền String
                    ingredients = ingredients,
                    instructions = instructions
                )

                // 3. Lưu vào Firestore (Remote)
                // Ta tự tạo ID mới cho Firestore
                val firestoreId = UUID.randomUUID().toString()

                saveRecipeToFirestore(
                    recipeId = firestoreId, // Truyền String UUID
                    name = name,
                    description = description,
                    timeCookMinutes = timeCookMinutes,
                    people = people,
                    imageUrl = imageUrl,
                    userId = currentUserId,
                    categoryId = categoryString,
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

    private suspend fun saveRecipeToFirestore(
        recipeId: String, // Long -> String
        name: String,
        description: String,
        timeCookMinutes: Int?,
        people: Int?,
        imageUrl: String?,
        userId: String, // Long -> String
        categoryId: String, // Long -> String
        ingredients: List<com.example.freshcookapp.domain.model.Ingredient>,
        instructions: List<com.example.freshcookapp.domain.model.Instruction>
    ) {
        val db = FirebaseFirestore.getInstance()
        // Tạo document chính
        val recipeRef = db.collection("recipes").document(recipeId)

        val safeTime = (timeCookMinutes ?: 0).toLong()
        val safePeople = (people ?: 1).toLong()
        // Dùng định dạng ngày chuẩn ISO hoặc Timestamp
        val createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

        val recipeData = hashMapOf(
            "id" to recipeId,
            "name" to name,
            "description" to description,
            "timeCook" to safeTime, // Khớp với ảnh bạn gửi (timeCook)
            "people" to safePeople,
            "imageUrl" to (imageUrl ?: ""),
            "userId" to userId,
            "categoryId" to categoryId,
            "difficulty" to "easy", // Mặc định, hoặc thêm logic chọn
            "createdAt" to createdAt
        )

        // 1. Lưu thông tin chính
        recipeRef.set(recipeData).await()

        // 2. Lưu Sub-collection: recipeIngredients (KHỚP VỚI ẢNH CỦA BẠN)
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

        // 3. Lưu Sub-collection: instruction (KHỚP VỚI ẢNH CỦA BẠN)
        val instructionCol = recipeRef.collection("instruction")
        instructions.forEach { ins ->
            val insData = hashMapOf(
                "step" to ins.stepNumber,
                "description" to ins.description,
                "imageUrl" to (ins.imageUrl ?: "")
            )
            instructionCol.add(insData).await()
        }

        Log.d("NewCookViewModel", "Đã lưu thành công lên Firestore với ID: $recipeId")
    }
}