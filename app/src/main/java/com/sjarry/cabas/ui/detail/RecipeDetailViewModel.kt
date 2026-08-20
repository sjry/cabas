package com.sjarry.cabas.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.dao.RecipeWithDetails
import com.sjarry.cabas.ui.RECIPE_ID_ARG
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RecipeDetailViewModel(
    repository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>(RECIPE_ID_ARG) ?: 0L

    val recipe: StateFlow<RecipeWithDetails?> = repository.observeRecipe(recipeId)
        .map { details ->
            details?.copy(
                ingredients = details.ingredients.sortedBy { it.position },
                steps = details.steps.sortedBy { it.position },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
