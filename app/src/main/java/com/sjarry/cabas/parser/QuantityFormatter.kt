package com.sjarry.cabas.parser

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Met en forme une quantité stockée dans son unité canonique (g ou ml) en
 * choisissant le multiple le plus lisible : 1250 g devient « 1,25 kg »,
 * 800 ml devient « 80 cl », 20 ml reste « 20 ml ».
 */
object QuantityFormatter {

    /** Quantité + unité, par exemple « 1,25 kg » ou « 3 cas » ou « 2 ». */
    fun format(quantity: Double, unit: IngredientUnit, freeLabel: String? = null): String {
        val amount = formatNumber(quantity)
        return when (unit) {
            IngredientUnit.GRAM -> when {
                quantity >= 1000.0 -> "${formatNumber(quantity / 1000.0)} kg"
                else -> "$amount g"
            }
            IngredientUnit.MILLILITER -> when {
                quantity >= 1000.0 -> "${formatNumber(quantity / 1000.0)} L"
                quantity >= 100.0 && isMultipleOf(quantity, 10.0) -> "${formatNumber(quantity / 10.0)} cl"
                else -> "$amount ml"
            }
            IngredientUnit.PIECE -> amount
            IngredientUnit.FREE -> if (freeLabel.isNullOrBlank()) amount else "$amount ${plural(freeLabel, quantity)}"
        }
    }

    /** Nombre à la française : virgule décimale, 2 décimales maximum, zéros inutiles retirés. */
    fun formatNumber(value: Double): String {
        val rounded = (value * 100.0).roundToLong() / 100.0
        if (abs(rounded - rounded.roundToLong()) < 1e-9) return rounded.roundToLong().toString()
        return String.format(Locale.FRANCE, "%.2f", rounded).trimEnd('0').trimEnd(',')
    }

    /** Abréviations invariables : « 3 cac », jamais « 3 cacs ». */
    private val INVARIABLE = setOf("cac", "cas")

    /** Accorde le libellé d'une unité libre au pluriel (« 3 pincées »). */
    private fun plural(label: String, quantity: Double): String {
        if (quantity < 2.0) return label
        if (label in INVARIABLE || label.endsWith('s') || label.contains('.')) return label
        return label + "s"
    }

    private fun isMultipleOf(value: Double, step: Double): Boolean =
        abs(value / step - (value / step).roundToLong()) < 1e-9
}
