package com.sjarry.cabas

import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.RecipeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeParserTest {

    private val sample = """
        # Poulet au curry

        ## Ingrédients
        - 150 g riz basmati
        - 1 blanc de poulet
        - 20 cl lait de coco
        - 1/2 oignon
        - 1 c. à café curry
        - sel

        ## Étapes
        1. Émincer l'oignon et le faire revenir.
        2. Ajouter le poulet coupé en dés.
        3. Verser le lait de coco, laisser mijoter 15 min.
    """.trimIndent()

    @Test
    fun `lit le titre les ingredients et les etapes`() {
        val recipe = RecipeParser.parse(sample)

        assertEquals("Poulet au curry", recipe.title)
        assertEquals(6, recipe.ingredients.size)
        assertEquals(3, recipe.steps.size)
        assertEquals("Émincer l'oignon et le faire revenir.", recipe.steps[0])
    }

    @Test
    fun `normalise les masses en grammes`() {
        val riz = RecipeParser.parse(sample).ingredients[0]
        assertEquals("riz basmati", riz.name)
        assertEquals(150.0, riz.quantity, 0.001)
        assertEquals(IngredientUnit.GRAM, riz.unit)
    }

    @Test
    fun `normalise les volumes en millilitres`() {
        val lait = RecipeParser.parse(sample).ingredients[2]
        assertEquals("lait de coco", lait.name)
        assertEquals(200.0, lait.quantity, 0.001)
        assertEquals(IngredientUnit.MILLILITER, lait.unit)
    }

    @Test
    fun `un ingredient sans unite est compte en pieces`() {
        val poulet = RecipeParser.parse(sample).ingredients[1]
        assertEquals("blanc de poulet", poulet.name)
        assertEquals(1.0, poulet.quantity, 0.001)
        assertEquals(IngredientUnit.PIECE, poulet.unit)
    }

    @Test
    fun `accepte les fractions`() {
        val oignon = RecipeParser.parse(sample).ingredients[3]
        assertEquals("oignon", oignon.name)
        assertEquals(0.5, oignon.quantity, 0.001)
        assertEquals(IngredientUnit.PIECE, oignon.unit)
    }

    @Test
    fun `conserve les unites libres avec leur libelle`() {
        val curry = RecipeParser.parse(sample).ingredients[4]
        assertEquals("curry", curry.name)
        assertEquals(1.0, curry.quantity, 0.001)
        assertEquals(IngredientUnit.FREE, curry.unit)
        assertEquals("cac", curry.freeUnitLabel)
    }

    @Test
    fun `un ingredient sans quantite est marque comme non chiffre`() {
        val sel = RecipeParser.parse(sample).ingredients[5]
        assertEquals("sel", sel.name)
        assertTrue(sel.unspecified)
    }

    @Test
    fun `convertit kilogrammes et litres`() {
        val ing = RecipeParser.parseIngredient("1,5 kg de pommes de terre")
        assertNotNull(ing)
        assertEquals(1500.0, ing!!.quantity, 0.001)
        assertEquals(IngredientUnit.GRAM, ing.unit)
        assertEquals("pommes de terre", ing.name)

        val litre = RecipeParser.parseIngredient("2 L d'eau")
        assertEquals(2000.0, litre!!.quantity, 0.001)
        assertEquals(IngredientUnit.MILLILITER, litre.unit)
        assertEquals("eau", litre.name)
    }

    @Test
    fun `accepte les nombres fractionnaires mixtes`() {
        val ing = RecipeParser.parseIngredient("1 1/2 c. à soupe d'huile d'olive")
        assertEquals(1.5, ing!!.quantity, 0.001)
        assertEquals(IngredientUnit.FREE, ing.unit)
        assertEquals("cas", ing.freeUnitLabel)
        assertEquals("huile d'olive", ing.name)
    }

    @Test
    fun `ne confond pas un nom commencant par une lettre d unite`() {
        val ing = RecipeParser.parseIngredient("2 gros oignons")
        assertEquals(IngredientUnit.PIECE, ing!!.unit)
        assertEquals("gros oignons", ing.name)

        val lait = RecipeParser.parseIngredient("3 laitues")
        assertEquals(IngredientUnit.PIECE, lait!!.unit)
        assertEquals("laitues", lait.name)
    }

    @Test
    fun `retire le balisage markdown du nom`() {
        val ing = RecipeParser.parseIngredient("100 g de **farine** T55")
        assertEquals("farine T55", ing!!.name)
    }

    @Test
    fun `accepte les sections sans accents et les puces variees`() {
        val md = """
            # Salade

            ## Ingredients
            * 50 g de mache
            + 1 tomate

            ## Preparation
            - Laver la mache.
        """.trimIndent()

        val recipe = RecipeParser.parse(md)
        assertEquals("Salade", recipe.title)
        assertEquals(2, recipe.ingredients.size)
        assertEquals(1, recipe.steps.size)
    }

    @Test
    fun `signale l absence de section ingredients sans planter`() {
        val recipe = RecipeParser.parse("# Vide\n\nDu texte libre.", fallbackTitle = "Vide")
        assertTrue(recipe.ingredients.isEmpty())
        assertTrue(recipe.warnings.isNotEmpty())
    }

    @Test
    fun `utilise le nom de fichier quand le titre manque`() {
        val recipe = RecipeParser.parse("## Ingrédients\n- 1 oeuf", fallbackTitle = "omelette")
        assertEquals("omelette", recipe.title)
        assertEquals(1, recipe.ingredients.size)
    }

    @Test
    fun `les cuilleres sont normalisees en cac et cas quelle que soit l ecriture`() {
        val ecritures = mapOf(
            "1 cac de sel" to "cac",
            "1 c. à café de sel" to "cac",
            "1 c.a.c. de sel" to "cac",
            "1 cuillère à café de sel" to "cac",
            "1 cas de sel" to "cas",
            "1 c. à soupe de sel" to "cas",
            "1 c.a.s. de sel" to "cas",
            "1 cuillères à soupe de sel" to "cas",
        )
        for ((ligne, attendu) in ecritures) {
            val ing = RecipeParser.parseIngredient(ligne)
            assertNotNull("non parsé : $ligne", ing)
            assertEquals(ligne, IngredientUnit.FREE, ing!!.unit)
            assertEquals(ligne, attendu, ing.freeUnitLabel)
            assertEquals(ligne, "sel", ing.name)
        }
    }

    @Test
    fun `un nom commencant par cac ou cas n est pas pris pour une cuillere`() {
        val cacahuetes = RecipeParser.parseIngredient("50 g de cacahuètes")
        assertEquals(IngredientUnit.GRAM, cacahuetes!!.unit)
        assertEquals("cacahuètes", cacahuetes.name)

        val cassonade = RecipeParser.parseIngredient("2 cassonades")
        assertEquals(IngredientUnit.PIECE, cassonade!!.unit)
        assertEquals("cassonades", cassonade.name)
    }

    @Test
    fun `ignore la mention optionnel`() {
        val ing = RecipeParser.parseIngredient("1 gousse d'ail (optionnel)")
        assertEquals("ail", ing!!.name)
    }
}
