package com.sjarry.cabas.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.dao.RecipeWithDetails
import com.sjarry.cabas.parser.ParsedSection
import com.sjarry.cabas.parser.RecipeParser
import com.sjarry.cabas.ui.DETAIL_DEFAULT_SERVINGS
import com.sjarry.cabas.ui.RECIPE_ID_ARG
import com.sjarry.cabas.ui.SERVINGS_ARG
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Ce que l'écran de détail affiche : la recette telle qu'elle est en base, et ses notes.
 *
 * Les notes — les sections du fichier que le parseur ne comprend pas — ne sont pas stockées :
 * aucune requête ne les concerne, et le markdown brut est déjà en base. On le relit ici, pour
 * une seule recette ; les recettes importées avant cette fonctionnalité en profitent donc sans
 * re-synchro ni migration.
 */
data class RecipeDetails(
    val details: RecipeWithDetails,
    val notes: List<ParsedSection>,
)

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

    val recipe: StateFlow<RecipeDetails?> = repository.observeRecipe(recipeId)
        .map { details ->
            details?.let {
                RecipeDetails(
                    details = it.copy(
                        ingredients = it.ingredients.sortedBy { i -> i.position },
                        steps = it.steps.sortedBy { s -> s.position },
                    ),
                    notes = RecipeParser.parse(it.recipe.rawMarkdown).notes,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
