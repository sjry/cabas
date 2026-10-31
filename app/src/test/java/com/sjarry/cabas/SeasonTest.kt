package com.sjarry.cabas

import com.sjarry.cabas.parser.Season
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeasonTest {

    @Test
    fun `reconnait chaque saison par son libelle`() {
        for (season in Season.entries) {
            assertEquals(setOf(season), Season.parse(season.label))
        }
    }

    @Test
    fun `ignore la casse les accents et les espaces`() {
        assertEquals(setOf(Season.SUMMER), Season.parse(" ETE "))
        assertEquals(setOf(Season.SUMMER), Season.parse("Été"))
        assertEquals(setOf(Season.WINTER), Season.parse("\"hiver\""))
        assertEquals(setOf(Season.AUTUMN), Season.parse("automnes"))
    }

    @Test
    fun `toutes vaut les quatre saisons`() {
        assertEquals(Season.entries.toSet(), Season.parse("toutes"))
        assertEquals(Season.entries.toSet(), Season.parse("Toute l'année"))
        assertEquals(Season.entries.toSet(), Season.parse("toutes saisons"))
    }

    @Test
    fun `rend null pour une valeur inconnue`() {
        assertNull(Season.parse("xyz"))
        assertNull(Season.parse(""))
        assertNull(Season.parse("mi-saison"))
    }

    @Test
    fun `lit une liste separee par des virgules des barres ou le mot et`() {
        assertEquals(setOf(Season.AUTUMN, Season.WINTER) to emptyList<String>(), Season.parseList("automne, hiver"))
        assertEquals(setOf(Season.SUMMER, Season.SPRING) to emptyList<String>(), Season.parseList("été et printemps"))
        assertEquals(setOf(Season.SUMMER, Season.AUTUMN) to emptyList<String>(), Season.parseList("été/automne"))
    }

    @Test
    fun `une liste garde les saisons comprises et rend les inconnues`() {
        val (known, unknown) = Season.parseList("hiver, xyz")
        assertEquals(setOf(Season.WINTER), known)
        assertEquals(listOf("xyz"), unknown)
    }

    @Test
    fun `present ne garde que les saisons rencontrees dans l ordre de l annee`() {
        val present = Season.present(
            listOf(setOf(Season.WINTER), emptySet(), setOf(Season.SPRING, Season.WINTER)),
        )
        assertEquals(listOf(Season.SPRING, Season.WINTER), present)
        assertEquals(emptyList<Season>(), Season.present(listOf(emptySet())))
    }

    @Test
    fun `le libelle enchaine les saisons dans l ordre de l annee`() {
        assertEquals("", Season.label(emptySet()))
        assertEquals("Hiver", Season.label(setOf(Season.WINTER)))
        assertEquals("Été, automne", Season.label(setOf(Season.AUTUMN, Season.SUMMER)))
        assertEquals("Toutes saisons", Season.label(Season.entries.toSet()))
    }
}
