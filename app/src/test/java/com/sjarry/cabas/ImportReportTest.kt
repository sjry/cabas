package com.sjarry.cabas

import com.sjarry.cabas.data.ImportReport
import com.sjarry.cabas.data.ImportWarning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportReportTest {

    private val warning = ImportWarning("Tarte", "Catégorie inconnue : « déssert »")

    @Test
    fun `resume un import simple`() {
        assertEquals("1 recette importée", ImportReport(imported = 1).summary())
        assertEquals("3 recettes importées, 2 mises à jour", ImportReport(imported = 3, updated = 2).summary())
    }

    @Test
    fun `compte les avertissements et les erreurs apres le resume`() {
        val report = ImportReport(imported = 1, warnings = listOf(warning, warning), errors = listOf("x"))
        assertEquals("1 recette importée · 2 avertissements · 1 erreur", report.summary())
    }

    @Test
    fun `une erreur sans rien d importe est le message lui-meme`() {
        assertEquals("Dossier introuvable.", ImportReport(errors = listOf("Dossier introuvable.")).summary())
    }

    @Test
    fun `rien d importe se dit`() {
        assertEquals("Aucune recette importée", ImportReport().summary())
        assertEquals("1 supprimée", ImportReport(removed = 1).summary())
    }

    @Test
    fun `il y a des details a lire des qu un avertissement ou une erreur existe`() {
        assertFalse(ImportReport(imported = 2).hasDetails)
        assertTrue(ImportReport(warnings = listOf(warning)).hasDetails)
        assertTrue(ImportReport(errors = listOf("x")).hasDetails)
    }
}
