package com.sjarry.cabas.ui.common

import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.QuantityFormatter
import com.sjarry.cabas.parser.RecipeCategory
import com.sjarry.cabas.parser.Season

/**
 * Libellé de quantité affiché à droite d'un article.
 * « qs » = quantité suffisante, pour les ingrédients que la recette ne chiffre pas.
 */
fun quantityLabel(
    quantity: Double,
    unit: IngredientUnit,
    freeUnitLabel: String?,
    unspecified: Boolean,
): String = when {
    unspecified -> "qs"
    unit == IngredientUnit.PIECE -> "× ${QuantityFormatter.formatNumber(quantity)}"
    else -> QuantityFormatter.format(quantity, unit, freeUnitLabel)
}

/** « 4 personnes », « 1 personne ». */
fun servingsLabel(servings: Int): String =
    if (servings > 1) "$servings personnes" else "$servings personne"

/** « 3 recettes », « 1 recette ». */
fun recipesLabel(count: Int): String =
    if (count > 1) "$count recettes" else "$count recette"

/** « Plat · Automne, hiver » ; null si la recette n'a ni catégorie ni saison. */
fun recipeTags(category: RecipeCategory?, seasons: Set<Season>): String? =
    listOfNotNull(category?.label, Season.label(seasons).ifEmpty { null })
        .joinToString(" · ")
        .ifEmpty { null }

/**
 * « Plat · Automne, hiver · soupe.md » : catégorie et saisons en tête du sous-titre d'une
 * recette, sur la même ligne. En premier parce que ce sont elles qui survivent à l'ellipse ;
 * sur une ligne pour ne pas faire grandir une liste de cent cinquante entrées.
 */
fun recipeSubtitle(category: RecipeCategory?, seasons: Set<Season>, text: String): String =
    listOfNotNull(recipeTags(category, seasons), text).joinToString(" · ")

/**
 * Message de l'état « aucun résultat », selon ce qui filtre : la saisie, la catégorie, la
 * saison, ou plusieurs à la fois. [byIngredients] précise que la recherche portait aussi sur
 * les ingrédients.
 */
fun noMatchMessage(
    query: String,
    category: RecipeCategory?,
    season: Season?,
    byIngredients: Boolean = false,
): String {
    val parts = listOfNotNull(
        query.takeIf { it.isNotBlank() }?.let { "à « $it »" },
        category?.let { "à la catégorie « ${it.label} »" },
        season?.let { "à la saison « ${it.label} »" },
    )
    val scope = if (byIngredients && query.isNotBlank()) ", ni par son titre ni par ses ingrédients" else ""
    val joined = when (parts.size) {
        0 -> return "Aucune recette."
        1 -> parts.single()
        else -> "à la fois " + parts.dropLast(1).joinToString(", ") + " et " + parts.last()
    }
    return "Aucune recette ne correspond $joined$scope."
}
