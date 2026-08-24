package com.sjarry.cabas.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sjarry.cabas.ui.detail.RecipeDetailScreen
import com.sjarry.cabas.ui.menu.MenuScreen
import com.sjarry.cabas.ui.recipes.RecipesScreen
import com.sjarry.cabas.ui.shopping.ShoppingScreen

const val RECIPE_ID_ARG = "recipeId"
const val SERVINGS_ARG = "servings"

/** Convives par défaut quand la recette est ouverte hors du menu : la recette telle qu'elle est écrite. */
const val DETAIL_DEFAULT_SERVINGS = 1

object Routes {
    const val RECIPES = "recipes"
    const val MENU = "menu"
    const val SHOPPING = "shopping"
    const val RECIPE_DETAIL = "recipe/{$RECIPE_ID_ARG}?$SERVINGS_ARG={$SERVINGS_ARG}"

    fun recipeDetail(id: Long, servings: Int = DETAIL_DEFAULT_SERVINGS) = "recipe/$id?$SERVINGS_ARG=$servings"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.RECIPES, "Recettes", Icons.AutoMirrored.Filled.MenuBook),
    Tab(Routes.MENU, "Menu", Icons.Filled.RestaurantMenu),
    Tab(Routes.SHOPPING, "Courses", Icons.Filled.Checklist),
)

@Composable
fun CabasApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.switchToTab(tab.route, currentRoute) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.RECIPES,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.RECIPES) {
                RecipesScreen(onOpenRecipe = { navController.navigate(Routes.recipeDetail(it)) })
            }
            composable(Routes.MENU) {
                MenuScreen(
                    onOpenShoppingList = { navController.switchToTab(Routes.SHOPPING, Routes.MENU) },
                    onGoToRecipes = { navController.switchToTab(Routes.RECIPES, Routes.MENU) },
                    onOpenRecipe = { id, servings ->
                        navController.navigate(Routes.recipeDetail(id, servings))
                    },
                )
            }
            composable(Routes.SHOPPING) {
                ShoppingScreen(onGoToMenu = { navController.switchToTab(Routes.MENU, Routes.SHOPPING) })
            }
            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = listOf(
                    navArgument(RECIPE_ID_ARG) { type = NavType.LongType },
                    navArgument(SERVINGS_ARG) {
                        type = NavType.IntType
                        defaultValue = DETAIL_DEFAULT_SERVINGS
                    },
                ),
            ) {
                RecipeDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * Bascule vers un onglet. Les trois onglets sont frères : ils ne doivent jamais
 * s'empiler les uns sur les autres, sinon `restoreState` réaffiche l'onglet
 * empilé au lieu de celui demandé. Tous les passages d'un onglet à l'autre —
 * barre du bas comme boutons dans les écrans — passent donc par ici.
 */
private fun NavHostController.switchToTab(route: String, currentRoute: String?) {
    if (currentRoute == route) return
    navigate(route) {
        popUpTo(Routes.RECIPES) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
