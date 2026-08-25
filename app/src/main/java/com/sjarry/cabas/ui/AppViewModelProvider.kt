package com.sjarry.cabas.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sjarry.cabas.AppContainer
import com.sjarry.cabas.CabasApplication
import com.sjarry.cabas.ui.detail.RecipeDetailViewModel
import com.sjarry.cabas.ui.menu.MenuViewModel
import com.sjarry.cabas.ui.menu.RecipePickerViewModel
import com.sjarry.cabas.ui.recipes.RecipesViewModel
import com.sjarry.cabas.ui.shopping.ShoppingViewModel

/** Fabrique unique des ViewModels : l'application se passe d'un injecteur. */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { RecipesViewModel(container().recipeRepository, container().settingsStore) }
        initializer { MenuViewModel(container().recipeRepository, container().menuRepository) }
        initializer { RecipePickerViewModel(container().recipeRepository, container().menuRepository) }
        initializer { ShoppingViewModel(container().menuRepository) }
        initializer { RecipeDetailViewModel(container().recipeRepository, createSavedStateHandle()) }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as CabasApplication).container
