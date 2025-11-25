package com.example.freshcookapp.data.repository

import com.example.freshcookapp.data.local.dao.CategoryDao
import com.example.freshcookapp.data.local.entity.CategoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryRepository(
    private val categoryDao: CategoryDao
) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncCategories() {
        val snapshot = firestore.collection("categories").get().await()

        val list = snapshot.documents.mapNotNull { doc ->
            CategoryEntity(
                id = doc.id,
                name = doc.getString("name") ?: "",
                imageUrl = doc.getString("imageUrl") ?: ""
            )
        }

        categoryDao.deleteAll()
        categoryDao.insertAll(list)
    }

    fun getLocalCategories() = categoryDao.getAllCategories()
}