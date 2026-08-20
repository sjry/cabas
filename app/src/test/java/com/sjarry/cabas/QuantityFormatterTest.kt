package com.sjarry.cabas

import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.QuantityFormatter.format
import com.sjarry.cabas.parser.QuantityFormatter.formatNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityFormatterTest {

    @Test
    fun `choisit le multiple le plus lisible pour les masses`() {
        assertEquals("600 g", format(600.0, IngredientUnit.GRAM))
        assertEquals("1,25 kg", format(1250.0, IngredientUnit.GRAM))
        assertEquals("2 kg", format(2000.0, IngredientUnit.GRAM))
    }

    @Test
    fun `choisit le multiple le plus lisible pour les volumes`() {
        assertEquals("20 ml", format(20.0, IngredientUnit.MILLILITER))
        assertEquals("80 cl", format(800.0, IngredientUnit.MILLILITER))
        assertEquals("1,5 L", format(1500.0, IngredientUnit.MILLILITER))
        assertEquals("125 ml", format(125.0, IngredientUnit.MILLILITER))
    }

    @Test
    fun `les pieces s affichent sans unite`() {
        assertEquals("3", format(3.0, IngredientUnit.PIECE))
        assertEquals("0,5", format(0.5, IngredientUnit.PIECE))
    }

    @Test
    fun `les unites libres s accordent au pluriel`() {
        assertEquals("3 pincées", format(3.0, IngredientUnit.FREE, "pincée"))
        // Les abréviations restent invariables.
        assertEquals("1 cac", format(1.0, IngredientUnit.FREE, "cac"))
        assertEquals("2 cas", format(2.0, IngredientUnit.FREE, "cas"))
        assertEquals("3 cac", format(3.0, IngredientUnit.FREE, "cac"))
    }

    @Test
    fun `les nombres utilisent la virgule et perdent les zeros inutiles`() {
        assertEquals("2", formatNumber(2.0))
        assertEquals("2,5", formatNumber(2.5))
        assertEquals("0,33", formatNumber(1.0 / 3.0))
        assertEquals("1,25", formatNumber(1.25))
    }
}
