package com.example.freshcookapp.ui.screen.search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.local.entity.RecipeEntity
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.Recipe
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchResultViewModel(
    private val keyword: String
) : ViewModel() {

    private val firestore = Firebase.firestore

    private val _results = MutableStateFlow<List<Recipe>>(emptyList())
    val results = _results.asStateFlow()

    init {
        loadResults()
    }

    private fun loadResults() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("recipes")
                    .whereGreaterThanOrEqualTo("name", keyword)
                    .whereLessThanOrEqualTo("name", keyword + "\uf8ff")
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val imageUrl = doc.getString("imageUrl")
                    val time = doc.getLong("timeCook")?.toInt() ?: 0

                    Recipe(
                        id = doc.id,
                        title = name,
                        imageUrl = imageUrl,
                        time = "$time phút",
                        level = "Dễ",
                        author = Author("Ẩn danh", "")
                    )
                }

                _results.value = list

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

