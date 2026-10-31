package com.sjarry.cabas.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.RecipeSearch
import com.sjarry.cabas.parser.RecipeCategory
import com.sjarry.cabas.parser.Season
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipePickerUiState(
    val query: String = "",
    /** Catégorie filtrée, toujours parmi [categories] ; null sans filtre. */
    val category: RecipeCategory? = null,
    /** Catégories des recettes hors menu : chaque puce rend donc au moins une recette. */
    val categories: List<RecipeCategory> = emptyList(),
    /** Saison filtrée, toujours parmi [seasons] ; null sans filtre. */
    val season: Season? = null,
    /** Saisons des recettes hors menu, même principe que [categories]. */
    val seasons: List<Season> = emptyList(),
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
    private val category = MutableStateFlow<RecipeCategory?>(null)
    private val season = MutableStateFlow<Season?>(null)

    val uiState: StateFlow<RecipePickerUiState> = combine(
        recipeRepository.observeCandidates(),
        menuRepository.observeMenuRecipeIds(),
        query,
        category,
        season,
    ) { candidates, inMenu, q, cat, seas ->
        val available = candidates.filter { it.id !in inMenu }
        val categories = RecipeCategory.present(available.map { it.category })
        val seasons = Season.present(available.map { it.seasons })
        // Un filtre dont la puce a disparu — sa dernière recette vient d'être ajoutée au menu
        // ou supprimée — cesse de filtrer, sinon la liste se vide sans explication.
        val selectedCategory = cat?.takeIf { it in categories }
        val selectedSeason = seas?.takeIf { it in seasons }
        RecipePickerUiState(
            query = q,
            category = selectedCategory,
            categories = categories,
            season = selectedSeason,
            seasons = seasons,
            sections = RecipeSearch.sections(candidates, inMenu, q, selectedCategory, selectedSeason),
            availableCount = available.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipePickerUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategoryChange(value: RecipeCategory?) {
        category.value = value
    }

    fun onSeasonChange(value: Season?) {
        season.value = value
    }

    fun addRecipes(ids: Set<Long>) = viewModelScope.launch {
        menuRepository.addRecipes(ids)
    }
}
