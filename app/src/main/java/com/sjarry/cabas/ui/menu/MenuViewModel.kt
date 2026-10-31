package com.sjarry.cabas.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.DrawCandidate
import com.sjarry.cabas.data.MenuDraw
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.model.MenuRecipe
import com.sjarry.cabas.parser.RecipeCategory
import com.sjarry.cabas.parser.Season
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MenuUiState(
    val menu: List<MenuRecipe> = emptyList(),
    val hasRecipes: Boolean = false,
    /** Recettes importées absentes du menu : ce que le tirage au sort a à sa disposition. */
    val candidates: List<DrawCandidate> = emptyList(),
    /**
     * Les recettes du menu que leur dé peut remplacer : il reste hors menu une recette de même
     * catégorie partageant une saison. Calculé ici, pas à l'appui : sans confirmation, un dé
     * sans effet serait indéchiffrable, il vaut mieux le griser.
     */
    val swappableIds: Set<Long> = emptySet(),
) {
    val isEmpty: Boolean get() = menu.isEmpty()
    val totalRecipes: Int get() = menu.size
    val availableCount: Int get() = candidates.size
}

class MenuViewModel(
    recipeRepository: RecipeRepository,
    private val menuRepository: MenuRepository,
) : ViewModel() {

    val uiState: StateFlow<MenuUiState> = combine(
        menuRepository.observeMenu(),
        recipeRepository.observeRecipes(),
    ) { menu, recipes ->
        val inMenu = menu.mapTo(mutableSetOf()) { it.recipeId }
        val candidates = recipes
            .filter { it.id !in inMenu }
            .map { DrawCandidate(it.id, it.category, it.seasons) }
        MenuUiState(
            menu = menu,
            hasRecipes = recipes.isNotEmpty(),
            candidates = candidates,
            swappableIds = menu
                .filter { MenuDraw.eligible(candidates, emptySet(), it.category, it.seasons).isNotEmpty() }
                .mapTo(mutableSetOf()) { it.recipeId },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    /**
     * Ajoute [count] recettes tirées au sort parmi celles qui ne sont pas déjà au menu,
     * toutes pour [servings] convives, dans la [category] et la [season] demandées — null
     * pour ne pas contraindre.
     */
    fun drawRandom(count: Int, servings: Int, category: RecipeCategory?, season: Season?) =
        viewModelScope.launch {
            menuRepository.addRandomRecipes(count, servings, category, setOfNotNull(season))
        }

    /** Remplace une recette du menu par une autre, tirée au sort. Sans confirmation : un appui suffit. */
    fun swapRecipe(recipeId: Long) = viewModelScope.launch {
        menuRepository.swapRecipe(recipeId)
    }

    fun changeServings(recipeId: Long, servings: Int) = viewModelScope.launch {
        menuRepository.setServings(recipeId, servings)
    }

    fun setDone(recipeId: Long, done: Boolean) = viewModelScope.launch {
        menuRepository.setDone(recipeId, done)
    }

    fun removeRecipe(recipeId: Long) = viewModelScope.launch {
        menuRepository.removeRecipe(recipeId)
    }

    fun clearMenu() = viewModelScope.launch { menuRepository.clearMenu() }
}
