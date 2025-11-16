package com.example.freshcookapp.ui.screen.newcook

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

        val recipeData = hashMapOf(
            "id" to recipeId,
            "name" to name,
            "description" to description,
            "timeCookMinutes" to timeCookMinutes,
            "people" to people,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "categoryId" to categoryId,
            "ingredients" to ingredientMaps,
            "instructions" to instructionMaps
        )

        recipeRef.set(recipeData).await()
    }
}
