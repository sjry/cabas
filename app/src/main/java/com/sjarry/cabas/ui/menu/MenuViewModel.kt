package com.sjarry.cabas.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.entities.RecipeEntity
import com.sjarry.cabas.data.model.MenuRecipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MenuUiState(
    val menu: List<MenuRecipe> = emptyList(),
    /** Recettes importées absentes du menu, proposées à l'ajout. */
    val available: List<RecipeEntity> = emptyList(),
    val hasRecipes: Boolean = false,
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
        menuRepository.observeMenuRecipeIds(),
    ) { menu, recipes, inMenu ->
        MenuUiState(
            menu = menu,
            available = recipes.filter { it.id !in inMenu },
            hasRecipes = recipes.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    fun addRecipes(ids: Set<Long>) = viewModelScope.launch {
        menuRepository.addRecipes(ids)
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
