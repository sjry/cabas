package com.sjarry.cabas.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.model.MenuRecipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MenuUiState(
    val menu: List<MenuRecipe> = emptyList(),
    val hasRecipes: Boolean = false,
    /** Recettes importées absentes du menu : ce que le tirage au sort a à sa disposition. */
    val availableCount: Int = 0,
) {
    val isEmpty: Boolean get() = menu.isEmpty()
    val totalRecipes: Int get() = menu.size
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
        MenuUiState(
            menu = menu,
            hasRecipes = recipes.isNotEmpty(),
            availableCount = recipes.count { it.id !in inMenu },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    /** Ajoute [count] recettes tirées au sort parmi celles qui ne sont pas déjà au menu. */
    fun drawRandom(count: Int) = viewModelScope.launch {
        menuRepository.addRandomRecipes(count)
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
