package com.sjarry.cabas

import com.sjarry.cabas.data.RecipeCandidate
import com.sjarry.cabas.data.RecipeSearch
import com.sjarry.cabas.data.ingredients
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeSearchTest {

    private fun candidate(id: Long, title: String, vararg ingredients: String) =
        RecipeCandidate(id, title, ingredients.joinToString(RecipeSearch.SEPARATOR))

    private val curry = candidate(1, "Poulet au curry", "riz basmati", "lait de coco", "curry")
    private val eclair = candidate(2, "Éclair au café", "pâte à choux", "café")
    private val chocolats = candidate(3, "3 chocolats", "chocolat noir", "crème")
    private val gratin = candidate(4, "Gratin de courgettes", "courgette", "crème")

    private val all = listOf(curry, eclair, chocolats, gratin)

    private fun titles(query: String, inMenu: Set<Long> = emptySet()): List<String> =
        RecipeSearch.sections(all, inMenu, query).flatMap { it.matches }.map { it.title }

    @Test
    fun `la recherche ignore la casse et les accents`() {
        assertEquals(listOf("Éclair au café"), titles("ECL"))
    }

    @Test
    fun `un ingredient fait remonter la recette et se dit en sous-titre`() {
        val matches = RecipeSearch.sections(all, emptySet(), "coco").single().matches
        assertEquals(listOf("Poulet au curry"), matches.map { it.title })
        assertEquals("lait de coco", matches.single().matchedIngredient)
    }

    @Test
    fun `un titre qui correspond n'affiche pas d'ingredient`() {
        val match = RecipeSearch.sections(all, emptySet(), "poulet").single().matches.single()
        assertNull(match.matchedIngredient)
    }

    @Test
    fun `le titre qui commence par la saisie passe devant celui qui la contient, puis les ingredients`() {
        val recipes = listOf(
            candidate(1, "Zeste de curry", "curry"),
            candidate(2, "Curry de légumes", "carotte"),
            candidate(3, "Poulet au lait de coco", "curry en poudre"),
        )
        val titles = RecipeSearch.sections(recipes, emptySet(), "curry")
            .single().matches.map { it.title }
        assertEquals(
            listOf("Curry de légumes", "Zeste de curry", "Poulet au lait de coco"),
            titles,
        )
    }

    @Test
    fun `tous les mots de la saisie doivent correspondre`() {
        assertEquals(listOf("Poulet au curry"), titles("curry poulet"))
        assertEquals(emptyList<String>(), titles("curry zèbre"))
    }

    @Test
    fun `sans recherche les recettes sont groupees par initiale, les accents a leur place`() {
        val sections = RecipeSearch.sections(all, emptySet(), "")
        assertEquals(listOf("E", "G", "P", "#"), sections.map { it.header })
        assertEquals(listOf("Éclair au café"), sections.first().matches.map { it.title })
    }

    @Test
    fun `un titre qui ne commence pas par une lettre va dans la derniere section`() {
        val last = RecipeSearch.sections(all, emptySet(), "").last()
        assertEquals(RecipeSearch.OTHER_HEADER, last.header)
        assertEquals(listOf("3 chocolats"), last.matches.map { it.title })
    }

    @Test
    fun `les recettes deja au menu sont ecartees`() {
        assertTrue(titles("", inMenu = setOf(1L)).none { it == "Poulet au curry" })
        assertEquals(emptyList<String>(), titles("coco", inMenu = setOf(1L)))
    }

    @Test
    fun `une recette sans ingredient ne compte aucun ingredient`() {
        val nue = candidate(9, "Tartine")
        assertEquals(emptyList<String>(), nue.ingredients())
        val match = RecipeSearch.sections(listOf(nue), emptySet(), "").single().matches.single()
        assertEquals(0, match.ingredientCount)
    }

    @Test
    fun `le nombre d'ingredients est celui de la recette`() {
        val match = RecipeSearch.sections(listOf(curry), emptySet(), "").single().matches.single()
        assertEquals(3, match.ingredientCount)
    }

    @Test
    fun `une recherche sans resultat ne rend aucune section`() {
        assertEquals(emptyList<RecipeSearch.Section>(), RecipeSearch.sections(all, emptySet(), "zzz"))
    }

    @Test
    fun `une saisie faite d'espaces equivaut a pas de recherche`() {
        assertEquals(
            RecipeSearch.sections(all, emptySet(), "").map { it.header },
            RecipeSearch.sections(all, emptySet(), "   ").map { it.header },
        )
    }
}
