package com.sjarry.cabas

import com.sjarry.cabas.parser.RecipeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeCategoryTest {

    @Test
    fun `reconnait chaque categorie par son libelle`() {
        for (category in RecipeCategory.entries) {
            assertEquals(category, RecipeCategory.parse(category.label))
        }
    }

    @Test
    fun `ignore la casse les accents et les espaces`() {
        assertEquals(RecipeCategory.DESSERT, RecipeCategory.parse("  DESSERT "))
        assertEquals(RecipeCategory.STARTER, RecipeCategory.parse("Entrée"))
        assertEquals(RecipeCategory.STARTER, RecipeCategory.parse("entree"))
        assertEquals(RecipeCategory.SNACK, RecipeCategory.parse("gouter"))
        assertEquals(RecipeCategory.MAIN, RecipeCategory.parse("\"plat\""))
    }

    @Test
    fun `accepte les synonymes et le pluriel`() {
        assertEquals(RecipeCategory.MAIN, RecipeCategory.parse("plat principal"))
        assertEquals(RecipeCategory.APERITIF, RecipeCategory.parse("apéro"))
        assertEquals(RecipeCategory.SNACK, RecipeCategory.parse("petit-déjeuner"))
        assertEquals(RecipeCategory.SNACK, RecipeCategory.parse("petit déjeuner"))
        assertEquals(RecipeCategory.DRINK, RecipeCategory.parse("boissons"))
        assertEquals(RecipeCategory.DESSERT, RecipeCategory.parse("Desserts"))
    }

    @Test
    fun `rend null pour une valeur inconnue`() {
        assertNull(RecipeCategory.parse("xyz"))
        assertNull(RecipeCategory.parse(""))
        assertNull(RecipeCategory.parse("   "))
        assertNull(RecipeCategory.parse("plateau de fromages"))
    }

    @Test
    fun `present ne garde que les categories rencontrees dans l ordre du repas`() {
        val present = RecipeCategory.present(
            listOf(RecipeCategory.DESSERT, null, RecipeCategory.STARTER, RecipeCategory.DESSERT),
        )
        assertEquals(listOf(RecipeCategory.STARTER, RecipeCategory.DESSERT), present)
        assertEquals(emptyList<RecipeCategory>(), RecipeCategory.present(listOf(null, null)))
    }
}
