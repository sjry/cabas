package com.sjarry.cabas.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.dao.RecipeWithDetails
import com.sjarry.cabas.ui.DETAIL_DEFAULT_SERVINGS
import com.sjarry.cabas.ui.RECIPE_ID_ARG
import com.sjarry.cabas.ui.SERVINGS_ARG
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RecipeDetailViewModel(
    repository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>(RECIPE_ID_ARG) ?: 0L

    /**
     * Convives pour lesquels afficher les quantités : celui choisi dans le menu
     * quand la recette y est ouverte, 1 (la recette telle qu'elle est écrite) sinon.
     */
    val servings: Int =
        savedStateHandle.get<Int>(SERVINGS_ARG)?.takeIf { it > 0 } ?: DETAIL_DEFAULT_SERVINGS

    val recipe: StateFlow<RecipeWithDetails?> = repository.observeRecipe(recipeId)
        .map { details ->
            details?.copy(
                ingredients = details.ingredients.sortedBy { it.position },
                steps = details.steps.sortedBy { it.position },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
