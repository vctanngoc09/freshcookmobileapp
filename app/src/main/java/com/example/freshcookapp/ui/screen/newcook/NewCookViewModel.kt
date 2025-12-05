package com.example.freshcookapp.ui.screen.newcook

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.domain.model.Ingredient
import com.example.freshcookapp.domain.model.Instruction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Locale

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

    fun saveRecipe(
        name: String,
        description: String,
        timeCook: Int?,
        people: Int?,
        imageUri: Uri?,
        videoUri: Uri?, // This parameter is no longer used but kept for compatibility
        hashtags: List<String>,
        difficultyUi: String,
        categoryId: String?,
        ingredients: List<Ingredient>,
        instructionsUi: List<InstructionUiState>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isUploading.value = true
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
                val recipeId = FirebaseFirestore.getInstance().collection("recipes").document().id
                val finalCategoryId = categoryId ?: "other"

                val difficulty = when (difficultyUi) {
                    "Dễ" -> "easy"
                    "Trung" -> "medium"
                    "Khó" -> "hard"
                    else -> "medium"
                }

                val imageTask = async { uploadRecipeImageIfNeeded(recipeId, imageUri) }
                val stepsTask = async { convertAndUploadInstructions(recipeId, instructionsUi) }

                val imageUrl = imageTask.await()
                val processedInstructions = stepsTask.await()

                saveRecipeToFirestore(
                    recipeId = recipeId,
                    name = name, description = description, timeCook = timeCook,
                    people = people, imageUrl = imageUrl, videoUrl = "", // Pass empty string for videoUrl
                    userId = currentUserId, categoryId = finalCategoryId,
                    hashtags = hashtags, difficulty = difficulty,
                    ingredients = ingredients, instructions = processedInstructions
                )

                isUploading.value = false
                onSuccess()
            } catch (e: Throwable) {
                isUploading.value = false
                Log.e("NewCookViewModel", "Lỗi lưu món", e)
                onError(e)
            }
        }
    }

    private suspend fun uploadRecipeImageIfNeeded(recipeId: String, imageUri: Uri?): String {
        if (imageUri == null) return ""
        return try {
            val ref = FirebaseStorage.getInstance().reference.child("recpies_img/$recipeId/main.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { "" }
    }

    private suspend fun convertAndUploadInstructions(
        recipeId: String,
        uiStates: List<InstructionUiState>
    ): List<Instruction> {
        return uiStates.mapIndexed { index, uiState ->
            viewModelScope.async {
                val imageUrls = mutableListOf<String>()

                val uploadJobs = uiState.imageUris.mapIndexed { imgIndex, uri ->
                    async {
                        try {
                            val ref = FirebaseStorage.getInstance().reference
                                .child("recpies_img/$recipeId/steps/step_${index}_img_$imgIndex.jpg")
                            ref.putFile(uri).await()
                            ref.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            ""
                        }
                    }
                }

                imageUrls.addAll(uploadJobs.awaitAll().filter { it.isNotEmpty() })
                val mainImage = imageUrls.firstOrNull() ?: ""

                Instruction(
                    stepNumber = index + 1,
                    description = uiState.description,
                    imageUrl = mainImage,
                    imageUrls = imageUrls
                )
            }
        }.awaitAll()
    }

    private suspend fun saveRecipeToFirestore(
        recipeId: String,
        name: String,
        description: String,
        timeCook: Int?,
        people: Int?,
        imageUrl: String,
        videoUrl: String,
        userId: String,
        categoryId: String,
        hashtags: List<String>,
        difficulty: String,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>
    ) {
        val db = FirebaseFirestore.getInstance()
        val recipeDoc = db.collection("recipes").document(recipeId)

        val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(java.util.Date())

        val normName = normalizeText(name)
        val nameParts = normName.split(" ").filter { it.isNotBlank() }
        val singleTokens = nameParts
        val pairTokens = nameParts.windowed(size = 2, step = 1).map { it.joinToString(" ") }
        val fullToken = listOf(normName)
        val compactToken = listOf(normName.replace(" ", ""))
        val ingTokens = ingredients
            .map { normalizeText(it.name) }
            .flatMap { it.split(" ") }
            .filter { it.isNotBlank() }

        val searchTokens = (singleTokens + pairTokens + fullToken + compactToken + ingTokens).distinct()

        val recipeData = hashMapOf(
            "id" to recipeId,
            "name" to name,
            "description" to description,
            "categoryId" to categoryId,
            "createdAt" to createdAt,
            "difficulty" to difficulty,
            "hashtagId" to hashtags,
            "imageUrl" to imageUrl,
            "videoUrl" to videoUrl,
            "likeCount" to 0,
            "people" to (people ?: 1),
            "timeCook" to (timeCook ?: 0),
            "userId" to userId,
            "searchTokens" to searchTokens
        )

        recipeDoc.set(recipeData).await()

        val tasks = mutableListOf<kotlinx.coroutines.Deferred<Any>>()

        instructions.forEach { step ->
            tasks.add(viewModelScope.async {
                val stepData = hashMapOf(
                    "step" to step.stepNumber,
                    "description" to step.description,
                    "imageUrl" to step.imageUrl,
                    "imageUrls" to step.imageUrls
                )
                recipeDoc.collection("instruction").add(stepData).await()
            })
        }

        ingredients.forEach { ing ->
            tasks.add(viewModelScope.async {
                val data = hashMapOf(
                    "name" to ing.name,
                    "quantity" to ing.quantity,
                    "unit" to ing.unit,
                    "note" to ing.notes
                )
                recipeDoc.collection("recipeIngredients").add(data).await()
            })
        }

        tasks.awaitAll()
    }
}