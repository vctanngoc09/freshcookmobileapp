package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.dao.RecipeIndexDao
import com.example.freshcookapp.data.local.entity.RecipeIndexEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncRepository(
    private val recipeIndexDao: RecipeIndexDao
) {

    private val firestore = Firebase.firestore

    suspend fun syncRecipeIndex() {
        val snapshot = firestore.collection("recipes").get().await()

        val indexList = snapshot.documents.map { doc ->
            RecipeIndexEntity(
                id = doc.id,
                name = doc.getString("name") ?: "Không tên"
            )
        }

        recipeIndexDao.insertAll(indexList)
    }
}
