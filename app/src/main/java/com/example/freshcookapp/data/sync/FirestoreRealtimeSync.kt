package com.example.freshcookapp.data.sync

import com.example.freshcookapp.data.local.dao.RecipeIndexDao
import com.example.freshcookapp.data.local.entity.RecipeIndexEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreRealtimeSync(
    private val indexDao: RecipeIndexDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    fun start() {
        firestore.collection("recipes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null

                    RecipeIndexEntity(
                        id = doc.id,              // ⭕ Firestore AUTO ID
                        name = name
                    )
                }

                CoroutineScope(Dispatchers.IO).launch {
                    indexDao.insertAll(list)
                }
            }
    }
}