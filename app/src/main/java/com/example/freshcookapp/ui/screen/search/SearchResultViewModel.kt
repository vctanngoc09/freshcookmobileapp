package com.example.freshcookapp.ui.screen.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.domain.model.Author
import com.example.freshcookapp.domain.model.Recipe
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.Normalizer

class SearchResultViewModel(
    private val keyword: String? = null,
    private val includedIngredients: List<String> = emptyList(),
    private val excludedIngredients: List<String> = emptyList(),
    private val difficulty: String = "",
    private val timeCook: Float = 0f
) : ViewModel() {

    private val TAG = "SearchResultVM"
    private val firestore = Firebase.firestore

    // Slider maximum (kept in sync with UI Filter slider max)
    private val SLIDER_TIME_MAX = 60f
    // Base tolerance used for small values
    private val BASE_TIME_TOLERANCE = 5f

    private val _results = MutableStateFlow<List<Recipe>>(emptyList())
    val results = _results.asStateFlow()

    init {
        loadResults()
    }

    private fun normalizeText(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .trim()
        return normalized
    }

    private fun difficultyMatches(recipeLevel: String, selected: String): Boolean {
        if (selected.isEmpty()) return true
        val r = normalizeText(recipeLevel)
        val s = normalizeText(selected)
        return when (s) {
            "kho", "hard" -> r.contains("hard") || r.contains("kho")
            "de", "easy" -> r.contains("easy") || r.contains("de")
            "trung", "medium", "normal" -> r.contains("medium") || r.contains("trung") || r.contains("normal")
            else -> r == s || r.contains(s)
        }
    }

    // Simple Levenshtein distance implementation for fuzzy matching (small strings only)
    private fun levenshteinDistance(s: String, t: String): Int {
        if (s == t) return 0
        if (s.isEmpty()) return t.length
        if (t.isEmpty()) return s.length

        val v0 = IntArray(t.length + 1) { it }
        val v1 = IntArray(t.length + 1)

        for (i in s.indices) {
            v1[0] = i + 1
            for (j in t.indices) {
                val cost = if (s[i] == t[j]) 0 else 1
                v1[j + 1] = minOf(
                    v1[j] + 1,
                    v0[j + 1] + 1,
                    v0[j] + cost
                )
            }
            for (k in v0.indices) v0[k] = v1[k]
        }
        return v1[t.length]
    }

    // Approximate match: true if query is substring OR small edit distance relative to length
    private fun approxMatches(token: String, query: String): Boolean {
        if (token.contains(query)) return true
        val maxDist = when {
            query.length <= 2 -> 0
            query.length <= 4 -> 1
            else -> maxOf(1, (query.length * 0.25).toInt())
        }
        val dist = levenshteinDistance(token, query)
        return dist <= maxDist
    }

    private fun loadResults() {
        viewModelScope.launch {
            try {
                // NOTE: to support fuzzy matching (typos in keyword) and ingredient-based search,
                // we fetch all recipes and filter client-side. For large datasets consider
                // switching to a server-side fuzzy index (Algolia, Elastic, Firestore Search Index, etc.).
                val query = firestore.collection("recipes")

                val snapshot = query.get().await()
                Log.d(TAG, "Fetched ${snapshot.size()} recipe documents")

                // For each recipe document, also fetch its recipeIngredients subcollection (if exists)
                val list = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val imageUrl = doc.getString("imageUrl")
                    val time = doc.getLong("timeCook")?.toInt() ?: 0
                    val difficultyDoc = doc.getString("difficulty") ?: "Dễ"

                    // Try to fetch subcollection recipeIngredients for this recipe
                    val ingredients: List<String> = try {
                        val ingSnapshot = firestore.collection("recipes")
                            .document(doc.id)
                            .collection("recipeIngredients")
                            .get()
                            .await()
                        val ingNames = ingSnapshot.documents.mapNotNull { ingDoc ->
                            ingDoc.getString("name")
                        }
                        Log.d(TAG, "Recipe ${doc.id} - ingredients from subcollection: $ingNames")
                        ingNames
                    } catch (e: Exception) {
                        // Fallback: maybe ingredients stored as array field on doc
                        Log.w(TAG, "Recipe ${doc.id} - failed to read subcollection: ${e.message}")
                        val arr = doc.get("ingredients")
                        @Suppress("UNCHECKED_CAST")
                        val a = (arr as? List<String>) ?: emptyList()
                        Log.d(TAG, "Recipe ${doc.id} - ingredients from field: $a")
                        a
                    }

                    Recipe(
                        id = doc.id,
                        title = name,
                        imageUrl = imageUrl,
                        time = "$time phút",
                        level = difficultyDoc,
                        author = Author("Ẩn danh", ""),
                        ingredients = ingredients
                    )
                }

                Log.d(TAG, "Mapped ${list.size} recipes after reading ingredients")

                val normIncluded = includedIngredients.map { normalizeText(it) }
                val normExcluded = excludedIngredients.map { normalizeText(it) }
                val normKeyword = keyword?.let { normalizeText(it) } ?: ""
                Log.d(TAG, "Normalized filters: keyword='$normKeyword' included=$normIncluded excluded=$normExcluded timeFilter=$timeCook")

                val filteredList = list.filter { recipe ->
                    // Combine title + ingredients tokens for matching (user may type recipe name or ingredient)
                    val tokens = listOf(recipe.title) + recipe.ingredients
                    val normTokens = tokens.map { normalizeText(it) }

                    // included ingredients: each included must match at least one token (approx)
                    val hasIncluded = normIncluded.isEmpty() || normIncluded.any { ing ->
                        normTokens.any { token -> approxMatches(token, ing) }
                    }

                    // excluded: none of the excluded should match tokens
                    val hasExcluded = normExcluded.isEmpty() || !normExcluded.any { ing ->
                        normTokens.any { token -> approxMatches(token, ing) }
                    }

                    val matchesDifficulty = difficultyMatches(recipe.level, difficulty)

                    val recipeTime = recipe.time.replace(" phút", "").toFloatOrNull() ?: 0f

                    // Improved time logic:
                    // - If timeCook == 0f -> no time filter
                    // - If user selected less than max -> match recipes that are <= selected + small tolerance
                    // - If user selected max (slider max) -> include recipes up to max + a larger tolerance (so slightly longer recipes are not excluded)
                    val scaledTolerance = if (timeCook <= 0f) BASE_TIME_TOLERANCE else maxOf(BASE_TIME_TOLERANCE, (timeCook * 0.1f))
                    val maxToleranceForMax = SLIDER_TIME_MAX * 0.5f // 30 when SLIDER_TIME_MAX=60

                    val matchesTime = when {
                        timeCook == 0f -> true
                        timeCook < SLIDER_TIME_MAX -> recipeTime <= timeCook + scaledTolerance
                        else -> recipeTime <= timeCook + maxToleranceForMax
                    }

                    // Keyword matching: if no keyword -> true. Otherwise check title and ingredients approx match
                    val matchesKeyword = normKeyword.isEmpty() || normTokens.any { token -> approxMatches(token, normKeyword) }

                    if (!hasIncluded || !hasExcluded || !matchesDifficulty || !matchesTime || !matchesKeyword) {
                        Log.d(TAG, "Recipe ${recipe.id} filtered OUT: hasIncluded=$hasIncluded hasExcluded=$hasExcluded matchesDifficulty=$matchesDifficulty matchesTime=$matchesTime matchesKeyword=$matchesKeyword ingredients=${recipe.ingredients} level=${recipe.level} time=$recipeTime")
                    } else {
                        Log.d(TAG, "Recipe ${recipe.id} kept")
                    }

                    hasIncluded && hasExcluded && matchesDifficulty && matchesTime && matchesKeyword
                }

                Log.d(TAG, "Filtered list size=${filteredList.size}")

                _results.value = filteredList

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error loading recipes: ${e.message}")
                _results.value = emptyList()
            }
        }
    }
}
