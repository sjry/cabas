package com.sjarry.cabas

import com.sjarry.cabas.data.MenuDraw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MenuDrawTest {

    private val library = (1L..20L).toList()

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
        assertEquals(emptyList<Long>(), MenuDraw.draw(library, library.toSet(), 3, Random(1)))
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
}
