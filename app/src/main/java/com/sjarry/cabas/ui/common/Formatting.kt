package com.sjarry.cabas.ui.common

import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.QuantityFormatter

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
