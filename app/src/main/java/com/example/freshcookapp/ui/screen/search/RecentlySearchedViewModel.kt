package com.example.freshcookapp.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcookapp.data.local.AppDatabase
import com.example.freshcookapp.data.repository.SearchRepository
import com.example.freshcookapp.data.repository.RecipeRepository
import com.example.freshcookapp.ui.screen.home.SuggestItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecentlySearchedViewModel(
    searchRepo: SearchRepository,
    recipeRepo: RecipeRepository
) : ViewModel() {

    val fullHistory: StateFlow<List<SuggestItem>> =
        searchRepo.getAllHistory()
            .flatMapLatest { items ->
                flow {
                    val list = items.map { history ->
                        val recipes = recipeRepo.searchRecipes(history.query)
                        val randomImage = recipes.first().randomOrNull()?.imageUrl
                        SuggestItem(
                            keyword = history.query,
                            timestamp = history.timestamp,
                            imageUrl = randomImage
                        )
                    }
                    emit(list)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
