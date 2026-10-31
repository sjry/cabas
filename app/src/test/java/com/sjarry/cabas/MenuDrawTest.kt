package com.sjarry.cabas

import com.sjarry.cabas.data.DrawCandidate
import com.sjarry.cabas.data.MenuDraw
import com.sjarry.cabas.parser.RecipeCategory
import com.sjarry.cabas.parser.Season
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MenuDrawTest {

    private val library = (1L..20L).map { DrawCandidate(it) }

    @Test
    fun `le tirage ne rend jamais une recette deja au menu`() {
        val inMenu = setOf(1L, 2L, 3L, 4L, 5L)
        // Assez de tirages pour que le hasard ait toutes ses chances de fauter.
        repeat(50) { seed ->
            val drawn = MenuDraw.draw(library, inMenu, count = 15, random = Random(seed))
            assertTrue(drawn.none { it in inMenu })
        }
    }

    @Test
    fun `le tirage ne rend jamais deux fois la meme recette`() {
        val drawn = MenuDraw.draw(library, emptySet(), count = 20, random = Random(7))
        assertEquals(drawn.size, drawn.toSet().size)
    }

    @Test
    fun `demander plus que disponible rend ce qui reste`() {
        val inMenu = (1L..18L).toSet()
        val drawn = MenuDraw.draw(library, inMenu, count = 10, random = Random(1))
        assertEquals(setOf(19L, 20L), drawn.toSet())
    }

    @Test
    fun `tout au menu ne rend rien`() {
        val all = library.map { it.id }.toSet()
        assertEquals(emptyList<Long>(), MenuDraw.draw(library, all, 3, Random(1)))
    }

    @Test
    fun `un nombre nul ou negatif ne rend rien`() {
        assertEquals(emptyList<Long>(), MenuDraw.draw(library, emptySet(), 0, Random(1)))
        assertEquals(emptyList<Long>(), MenuDraw.draw(library, emptySet(), -2, Random(1)))
    }

    @Test
    fun `une bibliotheque vide ne rend rien`() {
        assertEquals(emptyList<Long>(), MenuDraw.draw(emptyList(), emptySet(), 3, Random(1)))
    }

    @Test
    fun `a graine egale le tirage est reproductible`() {
        assertEquals(
            MenuDraw.draw(library, emptySet(), 3, Random(42)),
            MenuDraw.draw(library, emptySet(), 3, Random(42)),
        )
    }

    @Test
    fun `deux graines differentes donnent des tirages differents`() {
        // Sur 20 recettes, deux tirages de 3 identiques par hasard seraient un accident à
        // 1 sur 6840 : une seule paire de graines suffit à montrer que le tirage dépend bien
        // de la graine.
        assertNotEquals(
            MenuDraw.draw(library, emptySet(), 3, Random(1)),
            MenuDraw.draw(library, emptySet(), 3, Random(2)),
        )
    }

    // --- Catégorie et saison

    private val winterMain = DrawCandidate(1, RecipeCategory.MAIN, setOf(Season.WINTER))
    private val autumnWinterMain = DrawCandidate(2, RecipeCategory.MAIN, setOf(Season.AUTUMN, Season.WINTER))
    private val summerDessert = DrawCandidate(3, RecipeCategory.DESSERT, setOf(Season.SUMMER))
    private val allYearDessert = DrawCandidate(4, RecipeCategory.DESSERT, Season.entries.toSet())
    private val seasonlessMain = DrawCandidate(5, RecipeCategory.MAIN)
    private val bare = DrawCandidate(6)

    private val tagged = listOf(winterMain, autumnWinterMain, summerDessert, allYearDessert, seasonlessMain, bare)

    private fun ids(
        category: RecipeCategory? = null,
        seasons: Set<Season> = emptySet(),
        inMenu: Set<Long> = emptySet(),
    ) = MenuDraw.eligible(tagged, inMenu, category, seasons).map { it.id }.toSet()

    @Test
    fun `sans contrainte tout est eligible, toutes saisons et recettes sans saison comprises`() {
        assertEquals(setOf(1L, 2L, 3L, 4L, 5L, 6L), ids())
    }

    @Test
    fun `la categorie ecarte les autres categories et les recettes sans categorie`() {
        assertEquals(setOf(3L, 4L), ids(category = RecipeCategory.DESSERT))
        assertEquals(setOf(1L, 2L, 5L), ids(category = RecipeCategory.MAIN))
    }

    @Test
    fun `une saison demandee ecarte les recettes sans saison`() {
        assertEquals(setOf(1L, 2L, 4L), ids(seasons = setOf(Season.WINTER)))
    }

    @Test
    fun `une recette toutes saisons sort sous chaque saison demandee`() {
        for (season in Season.entries) {
            assertTrue(season.name, allYearDessert.id in ids(seasons = setOf(season)))
        }
    }

    @Test
    fun `il suffit d une saison en commun, comme pour le de d une carte`() {
        // La recette remplacée est automne-hiver : un plat d'hiver convient, un plat d'été non.
        val replacing = ids(category = RecipeCategory.MAIN, seasons = autumnWinterMain.seasons, inMenu = setOf(2L))
        assertEquals(setOf(1L), replacing)
        // Et une recette toutes saisons est toujours retenue, quelle que soit la saison demandée.
        assertTrue(allYearDessert.id in ids(category = RecipeCategory.DESSERT, seasons = setOf(Season.AUTUMN)))
    }

    @Test
    fun `categorie et saison se cumulent et peuvent ne rien laisser`() {
        assertEquals(setOf(4L), ids(category = RecipeCategory.DESSERT, seasons = setOf(Season.WINTER)))
        assertEquals(emptySet<Long>(), ids(category = RecipeCategory.MAIN, seasons = setOf(Season.SUMMER)))
    }

    @Test
    fun `le tirage contraint ne sort que des eligibles`() {
        repeat(20) { seed ->
            val drawn = MenuDraw.draw(tagged, emptySet(), 3, Random(seed), RecipeCategory.MAIN, setOf(Season.WINTER))
            assertEquals(setOf(1L, 2L), drawn.toSet())
        }
    }
}
