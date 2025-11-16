package com.example.freshcookapp.ui.screen.newcook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.repository.RecipeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NewCookViewModel(private val recipeRepository: RecipeRepository) : ViewModel() {

    fun saveRecipe(
        name: String,
        description: String,
        timeCookMinutes: Int?,
        people: Int?,
        imageUrl: String?,
        userId: Long,
        categoryId: Long,
        ingredients: List<com.example.freshcookapp.domain.model.Ingredient>,
        instructions: List<com.example.freshcookapp.domain.model.Instruction>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val recipeId = recipeRepository.saveRecipe(
                    name = name,
                    description = description,
                    timeCookMinutes = timeCookMinutes,
                    people = people,
                    imageUrl = imageUrl,
                    userId = userId,
                    categoryId = categoryId,
                    ingredients = ingredients,
                    instructions = instructions
                )

                // Save to Firebase Firestore
                saveRecipeToFirestore(
                    recipeId = recipeId,
                    name = name,
                    description = description,
                    timeCookMinutes = timeCookMinutes,
                    people = people,
                    imageUrl = imageUrl,
                    userId = userId,
                    categoryId = categoryId,
                    ingredients = ingredients,
                    instructions = instructions
                )

                onSuccess()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    private suspend fun saveRecipeToFirestore(
        recipeId: Long,
        name: String,
        description: String,
        timeCookMinutes: Int?,
        people: Int?,
        imageUrl: String?,
        userId: Long,
        categoryId: Long,
        ingredients: List<com.example.freshcookapp.domain.model.Ingredient>,
        instructions: List<com.example.freshcookapp.domain.model.Instruction>
    ) {
        val db = FirebaseFirestore.getInstance()
        val recipeRef = db.collection("recipes").document(recipeId.toString())

        val ingredientMaps = ingredients.map { ingredient ->
            hashMapOf(
                "name" to ingredient.name,
                "quantity" to ingredient.quantity,
                "unit" to ingredient.unit,
                "notes" to ingredient.notes
            )
        }

        val instructionMaps = instructions.map { instruction ->
            hashMapOf(
                "stepNumber" to instruction.stepNumber,
                "description" to instruction.description,
                "imageUrl" to instruction.imageUrl
            )
        }

        // ensure non-null numeric fields before writing to Firestore
        val safeTime = (timeCookMinutes ?: 0).toLong()
        val safePeople = (people ?: 1).toLong()

        val recipeData = hashMapOf(
            "id" to recipeId,
            "name" to name,
            "description" to description,
            "timeCookMinutes" to safeTime,
            "people" to safePeople,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "categoryId" to categoryId,
            "ingredients" to ingredientMaps,
            "instructions" to instructionMaps
        )

        Log.d("NewCookViewModel", "Will save recipe to Firestore (id=${recipeId}): timeCookMinutes=$safeTime, people=$safePeople")
        Log.d("NewCookViewModel", "recipeData=$recipeData")

        recipeRef.set(recipeData).await()

        // Read back the document and log its data to verify what was saved
        val snapshot = recipeRef.get().await()
        if (snapshot.exists()) {
            val saved = snapshot.data
            Log.d("NewCookViewModel", "Read back doc after save: id=${snapshot.id}, data=$saved")
            val savedTime = snapshot.getLong("timeCookMinutes")
            Log.d("NewCookViewModel", "Read back timeCookMinutes (from Firestore) = $savedTime")
        } else {
            Log.w("NewCookViewModel", "Document not found right after set()")
        }

        Log.d("NewCookViewModel", "Saved recipe to Firestore (id=${recipeId})")
    }
}
