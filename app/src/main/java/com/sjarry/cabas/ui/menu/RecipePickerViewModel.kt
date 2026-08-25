package com.sjarry.cabas.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.RecipeSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipePickerUiState(
    val query: String = "",
    /** Sections par initiale, ou résultats classés par pertinence si une recherche est en cours. */
    val sections: List<RecipeSearch.Section> = emptyList(),
    /**
     * Recettes hors menu avant filtrage. Distingue les deux vides de l'écran : « tout est déjà au
     * menu » n'est pas « aucun résultat ».
     */
    val availableCount: Int = 0,
)

/**
 * L'écran de sélection a son propre ViewModel, et non celui du menu : sa recherche naît et meurt
 * avec la destination, il n'y a donc rien à remettre à zéro en sortant, et l'écran Menu n'a pas à
 * recalculer une liste qu'il n'affiche pas.
 */
class RecipePickerViewModel(
    recipeRepository: RecipeRepository,
    private val menuRepository: MenuRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<RecipePickerUiState> = combine(
        recipeRepository.observeCandidates(),
        menuRepository.observeMenuRecipeIds(),
        query,
    ) { candidates, inMenu, q ->
        RecipePickerUiState(
            query = q,
            sections = RecipeSearch.sections(candidates, inMenu, q),
            availableCount = candidates.count { it.id !in inMenu },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipePickerUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun addRecipes(ids: Set<Long>) = viewModelScope.launch {
        menuRepository.addRecipes(ids)
    }
}
