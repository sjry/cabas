package com.sjarry.cabas

import com.sjarry.cabas.data.ShoppingListBuilder
import com.sjarry.cabas.data.model.MenuRecipe
import com.sjarry.cabas.parser.IngredientCategory
import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.ParsedIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListBuilderTest {

    private fun g(name: String, q: Double) =
        ParsedIngredient(name, q, IngredientUnit.GRAM)

    private fun ml(name: String, q: Double) =
        ParsedIngredient(name, q, IngredientUnit.MILLILITER)

    private fun piece(name: String, q: Double) =
        ParsedIngredient(name, q, IngredientUnit.PIECE)

    private val curry = MenuRecipe(
        recipeId = 1,
        title = "Poulet au curry",
        servings = 4,
        ingredients = listOf(
            g("riz basmati", 150.0),
            ml("lait de coco", 200.0),
            piece("blanc de poulet", 1.0),
        ),
    )

    private val salade = MenuRecipe(
        recipeId = 2,
        title = "Salade",
        servings = 2,
        ingredients = listOf(
            g("Riz basmati", 50.0),
            piece("tomate", 2.0),
        ),
    )

    @Test
    fun `multiplie les quantites par le nombre de convives`() {
        val items = ShoppingListBuilder.aggregate(listOf(curry))
        val riz = items.first { it.name == "riz basmati" }
        assertEquals(600.0, riz.quantity, 0.001)
        val lait = items.first { it.name == "lait de coco" }
        assertEquals(800.0, lait.quantity, 0.001)
    }

    @Test
    fun `fusionne un ingredient present dans deux recettes`() {
        val items = ShoppingListBuilder.aggregate(listOf(curry, salade))
        val riz = items.filter { ShoppingListBuilder.normalizeName(it.name) == "riz basmati" }
        assertEquals(1, riz.size)
        // 150 x 4 convives + 50 x 2 convives
        assertEquals(700.0, riz.first().quantity, 0.001)
    }

    @Test
    fun `trie la vue totale par ordre alphabetique`() {
        val names = ShoppingListBuilder.aggregate(listOf(curry, salade)).map { it.name }
        assertEquals(listOf("blanc de poulet", "lait de coco", "riz basmati", "tomate"), names)
    }

    @Test
    fun `ne melange pas masse et volume d un meme ingredient`() {
        val menu = listOf(
            MenuRecipe(1, "A", 1, listOf(g("beurre", 100.0))),
            MenuRecipe(2, "B", 1, listOf(ml("beurre", 20.0))),
        )
        assertEquals(2, ShoppingListBuilder.aggregate(menu).size)
    }

    @Test
    fun `la vue par recette conserve les quantites multipliees`() {
        val sections = ShoppingListBuilder.sections(listOf(curry, salade))
        assertEquals(2, sections.size)
        assertEquals("Poulet au curry", sections[0].title)
        assertEquals(4, sections[0].servings)
        assertEquals(600.0, sections[0].items.first { it.name == "riz basmati" }.quantity, 0.001)
        assertEquals(100.0, sections[1].items.first().quantity, 0.001)
    }

    @Test
    fun `les deux vues partagent la meme cle de case a cocher`() {
        val key = ShoppingListBuilder.aggregate(listOf(curry, salade))
            .first { it.name == "riz basmati" }.key
        val sectionKeys = ShoppingListBuilder.sections(listOf(curry, salade))
            .flatMap { it.items }
            .filter { ShoppingListBuilder.normalizeName(it.name) == "riz basmati" }
            .map { it.key }
        assertTrue(sectionKeys.isNotEmpty())
        assertTrue(sectionKeys.all { it == key })
    }

    @Test
    fun `reporte l etat coche sur les articles`() {
        val list = ShoppingListBuilder.build(listOf(curry))
        val rizKey = list.total.first { it.name == "riz basmati" }.key
        val checked = ShoppingListBuilder.build(listOf(curry), setOf(rizKey))
        assertTrue(checked.total.first { it.key == rizKey }.checked)
        assertFalse(checked.total.first { it.name == "tomate" || it.name == "lait de coco" }.checked)
        assertEquals(1, checked.checkedCount)
        assertEquals(3, checked.itemCount)
    }

    @Test
    fun `un ingredient sans quantite reste une ligne unique`() {
        val sel = ParsedIngredient("sel", 1.0, IngredientUnit.PIECE, unspecified = true)
        val menu = listOf(
            MenuRecipe(1, "A", 4, listOf(sel)),
            MenuRecipe(2, "B", 2, listOf(sel)),
        )
        val items = ShoppingListBuilder.aggregate(menu)
        assertEquals(1, items.size)
        assertTrue(items.first().unspecified)
    }

    @Test
    fun `un ingredient non chiffre disparait si une recette le chiffre`() {
        val selQs = ParsedIngredient("sel", 1.0, IngredientUnit.PIECE, unspecified = true)
        val selPincee = ParsedIngredient("sel", 1.0, IngredientUnit.FREE, freeUnitLabel = "pincée")
        val menu = listOf(
            MenuRecipe(1, "A", 4, listOf(selQs)),
            MenuRecipe(2, "B", 6, listOf(selPincee)),
        )
        val items = ShoppingListBuilder.aggregate(menu)
        assertEquals(1, items.size)
        assertEquals("pincée", items.first().freeUnitLabel)
        assertEquals(6.0, items.first().quantity, 0.001)
    }

    @Test
    fun `un menu vide donne une liste vide`() {
        assertTrue(ShoppingListBuilder.build(emptyList()).isEmpty)
    }

    @Test
    fun `un article pris quitte la liste principale pour la section des pris`() {
        val riz = ShoppingListBuilder.keyOf(g("riz basmati", 150.0))
        val list = ShoppingListBuilder.build(listOf(curry), setOf(riz))

        assertEquals(2, list.remaining.size)
        assertTrue(list.remaining.none { it.key == riz })
        assertEquals(listOf(riz), list.taken.map { it.key })
        assertFalse(list.isComplete)
    }

    @Test
    fun `une liste entierement cochee est complete et n a plus rien a prendre`() {
        val keys = ShoppingListBuilder.aggregate(listOf(curry, salade)).map { it.key }.toSet()
        val list = ShoppingListBuilder.build(listOf(curry, salade), keys)

        assertTrue(list.isComplete)
        assertTrue(list.remaining.isEmpty())
        assertEquals(list.itemCount, list.taken.size)
    }

    @Test
    fun `une liste vide n est pas une liste complete`() {
        assertFalse(ShoppingListBuilder.build(emptyList()).isComplete)
    }

    @Test
    fun `une recette dont tout est pris n a plus rien a prendre`() {
        val keys = curry.ingredients.map { ShoppingListBuilder.keyOf(it) }.toSet()
        val sections = ShoppingListBuilder.sections(listOf(curry, salade), keys)

        assertTrue(sections.first { it.recipeId == 1L }.remaining.isEmpty())
        // « riz basmati » est partagé : la salade en a un de coché, pas les deux.
        assertEquals(1, sections.first { it.recipeId == 2L }.remaining.size)
    }

    @Test
    fun `groupe la liste par rayon dans l ordre du parcours`() {
        val aisles = ShoppingListBuilder.build(listOf(curry, salade)).aisles

        assertEquals(
            listOf(
                IngredientCategory.PRODUCE,
                IngredientCategory.BUTCHER,
                IngredientCategory.GROCERY,
            ),
            aisles.map { it.category },
        )
        assertEquals(listOf("tomate"), aisles[0].items.map { it.name })
        assertEquals(listOf("blanc de poulet"), aisles[1].items.map { it.name })
        // « lait de coco » est en épicerie, pas en crémerie, et l'ordre alphabétique
        // de la vue « Total » se retrouve à l'intérieur du rayon.
        assertEquals(listOf("lait de coco", "riz basmati"), aisles[2].items.map { it.name })
    }

    @Test
    fun `un rayon vide de sa derniere ligne n a plus rien a prendre`() {
        val tomate = ShoppingListBuilder.keyOf(piece("tomate", 2.0))
        val aisles = ShoppingListBuilder.build(listOf(curry, salade), setOf(tomate)).aisles

        assertTrue(aisles.first { it.category == IngredientCategory.PRODUCE }.remaining.isEmpty())
        assertEquals(2, aisles.first { it.category == IngredientCategory.GROCERY }.remaining.size)
    }

    @Test
    fun `une correction de rayon deplace l article dans les deux vues`() {
        val overrides = mapOf("riz basmati" to IngredientCategory.OTHER)
        val list = ShoppingListBuilder.build(listOf(curry, salade), emptySet(), overrides)

        assertEquals(IngredientCategory.OTHER, list.total.first { it.name == "riz basmati" }.category)
        assertTrue(
            list.sections.flatMap { it.items }
                .filter { ShoppingListBuilder.normalizeName(it.name) == "riz basmati" }
                .all { it.category == IngredientCategory.OTHER },
        )
        // « Divers » ferme la marche.
        assertEquals(IngredientCategory.OTHER, list.aisles.last().category)
    }

    @Test
    fun `corriger le rayon ne change pas la cle de l article`() {
        val riz = ShoppingListBuilder.build(listOf(curry)).total.first { it.name == "riz basmati" }
        val overrides = mapOf("riz basmati" to IngredientCategory.FROZEN)
        val corrige = ShoppingListBuilder.build(listOf(curry), setOf(riz.key), overrides)
            .total.first { it.name == "riz basmati" }

        assertEquals(riz.key, corrige.key)
        assertTrue(corrige.checked)
    }
}
