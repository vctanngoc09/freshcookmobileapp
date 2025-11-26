package com.example.freshcookapp.util

object FilterStore {
    var includedIngredients: List<String> = emptyList()
    var excludedIngredients: List<String> = emptyList()
    var difficulty: String = ""
    var timeCook: Float = 0f

    fun setFilters(included: List<String>, excluded: List<String>, diff: String, time: Float) {
        includedIngredients = included
        excludedIngredients = excluded
        difficulty = diff
        timeCook = time
    }

    fun clear() {
        includedIngredients = emptyList()
        excludedIngredients = emptyList()
        difficulty = ""
        timeCook = 0f
    }
}

