package com.sjarry.cabas.data.model

import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.ParsedIngredient

/** Une recette du menu avec son nombre de convives. */
data class MenuRecipe(
    val recipeId: Long,
    val title: String,
    val servings: Int,
    /** Ingrédients pour 1 personne. */
    val ingredients: List<ParsedIngredient>,
    /** Recette déjà cuisinée. N'influe pas sur la liste de courses. */
    val done: Boolean = false,
)

/** Une ligne de la liste de courses. */
data class ShoppingItem(
    /** Clé stable partagée par les deux vues : c'est elle qui porte la case à cocher. */
    val key: String,
    val name: String,
    val quantity: Double,
    val unit: IngredientUnit,
    val freeUnitLabel: String? = null,
    val unspecified: Boolean = false,
    val checked: Boolean = false,
)

/** Bloc « une recette » de la vue détaillée. */
data class RecipeSection(
    val recipeId: Long,
    val title: String,
    val servings: Int,
    val items: List<ShoppingItem>,
)

/** Liste de courses complète, dans ses deux présentations. */
data class ShoppingList(
    val total: List<ShoppingItem>,
    val sections: List<RecipeSection>,
) {
    val itemCount: Int get() = total.size
    val checkedCount: Int get() = total.count { it.checked }
    val isEmpty: Boolean get() = total.isEmpty()
}
