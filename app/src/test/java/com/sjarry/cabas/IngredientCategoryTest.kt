package com.sjarry.cabas

import com.sjarry.cabas.data.ShoppingListBuilder.categoryOf
import com.sjarry.cabas.parser.IngredientCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientCategoryTest {

    @Test
    fun `classe les ingredients courants dans leur rayon`() {
        assertEquals(IngredientCategory.PRODUCE, categoryOf("oignon"))
        assertEquals(IngredientCategory.BUTCHER, categoryOf("blanc de poulet"))
        assertEquals(IngredientCategory.DAIRY, categoryOf("crème fraîche"))
        assertEquals(IngredientCategory.BAKERY, categoryOf("pain de mie"))
        assertEquals(IngredientCategory.GROCERY, categoryOf("riz basmati"))
    }

    @Test
    fun `un ingredient inconnu tombe dans divers`() {
        assertEquals(IngredientCategory.OTHER, categoryOf("poudre de perlimpinpin"))
    }

    @Test
    fun `ignore la casse et les accents`() {
        assertEquals(IngredientCategory.PRODUCE, categoryOf("ÉCHALOTE"))
        assertEquals(IngredientCategory.GROCERY, categoryOf("Farine T55"))
    }

    @Test
    fun `reconnait la ligature du mot oeuf`() {
        assertEquals(IngredientCategory.DAIRY, categoryOf("œufs"))
        assertEquals(IngredientCategory.DAIRY, categoryOf("oeufs"))
    }

    @Test
    fun `ne reconnait un mot cle qu en entier`() {
        // « ail » ne doit pas être vu dans « cocktail ».
        assertEquals(IngredientCategory.OTHER, categoryOf("cocktail"))
    }

    // Les trois cas qui justifient la règle du plus long mot-clé : le cas particulier
    // doit l'emporter sur le mot générique qu'il contient.

    @Test
    fun `lait de coco va en epicerie pas en cremerie`() {
        assertEquals(IngredientCategory.DAIRY, categoryOf("lait"))
        assertEquals(IngredientCategory.GROCERY, categoryOf("lait de coco"))
    }

    @Test
    fun `tomates pelees vont en epicerie mais les tomates au frais`() {
        assertEquals(IngredientCategory.PRODUCE, categoryOf("tomates"))
        assertEquals(IngredientCategory.GROCERY, categoryOf("tomates pelées"))
        assertEquals(IngredientCategory.GROCERY, categoryOf("concentré de tomates"))
    }

    @Test
    fun `sucre glace reste en epicerie malgre le mot glace`() {
        assertEquals(IngredientCategory.FROZEN, categoryOf("glace"))
        assertEquals(IngredientCategory.GROCERY, categoryOf("sucre glace"))
    }

    @Test
    fun `un ingredient surgele va aux surgeles quel qu il soit`() {
        assertEquals(IngredientCategory.PRODUCE, categoryOf("petits pois"))
        assertEquals(IngredientCategory.FROZEN, categoryOf("petits pois surgelés"))
        assertEquals(IngredientCategory.FROZEN, categoryOf("épinards congelés"))
    }

    @Test
    fun `une correction apprise l emporte sur le lexique`() {
        val overrides = mapOf("lait de coco" to IngredientCategory.OTHER)
        assertEquals(IngredientCategory.OTHER, categoryOf("Lait de coco", overrides))
        // La correction est rangée sous le nom normalisé : elle vaut aussi pour les
        // écritures voisines du même ingrédient.
        assertEquals(IngredientCategory.OTHER, categoryOf("  LAIT   de coco ", overrides))
    }
}
