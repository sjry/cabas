package com.sjarry.cabas.parser

/** Un ingrédient tel que lu dans le markdown, pour 1 personne. */
data class ParsedIngredient(
    val name: String,
    val quantity: Double,
    val unit: IngredientUnit,
    val freeUnitLabel: String? = null,
    /** Vrai quand la recette ne donne pas de quantité (« sel », « poivre »). */
    val unspecified: Boolean = false,
    val rawLine: String = "",
)

/** Résultat du parsing d'un fichier markdown. */
data class ParsedRecipe(
    val title: String,
    val ingredients: List<ParsedIngredient>,
    val steps: List<String>,
    /** Anomalies non bloquantes à afficher après l'import. */
    val warnings: List<String>,
)

/**
 * Parseur du format de recette maison : un titre `#`, une section `## Ingrédients`
 * et une section `## Étapes`. Volontairement tolérant — une ligne mal formée est
 * conservée telle quelle et signalée plutôt que perdue.
 */
object RecipeParser {

    private val TITLE = Regex("""^\s{0,3}#\s+(.*\S)\s*$""")
    private val HEADING = Regex("""^\s{0,3}(#{2,6})\s+(.*\S)\s*$""")
    private val BULLET = Regex("""^\s*[-*+]\s+(.*\S)\s*$""")
    private val NUMBERED = Regex("""^\s*\d+\s*[.)]\s+(.*\S)\s*$""")

    /** Quantité en tête de ligne : entier, décimal (`1.5`/`1,5`), fraction (`1/2`, `1 1/2`). */
    private val QUANTITY = Regex("""^\s*(\d+\s+\d+\s*/\s*\d+|\d+\s*/\s*\d+|\d+(?:[.,]\d+)?)\s*""")

    /** Mots de liaison à retirer entre l'unité et le nom : « 100 g de farine ». */
    private val CONNECTOR = Regex("""^(?:d'|d’|de\s+la\s+|de\s+l'|de\s+l’|des\s+|du\s+|de\s+)""", RegexOption.IGNORE_CASE)

    /** Marqueur de fin de ligne : « + optionnel », « (facultatif) ». */
    private val OPTIONAL = Regex("""\s*[(\[]?\s*(?:\+\s*)?(?:optionnel|facultatif)(?:le)?s?\s*[)\]]?\s*$""", RegexOption.IGNORE_CASE)

    private enum class Section { NONE, INGREDIENTS, STEPS, OTHER }

    /**
     * @param markdown contenu brut du fichier
     * @param fallbackTitle titre utilisé si le markdown n'en contient pas
     */
    fun parse(markdown: String, fallbackTitle: String = "Sans titre"): ParsedRecipe {
        val lines = markdown.replace("\r\n", "\n").replace("\r", "\n").split("\n")

        var title: String? = null
        var section = Section.NONE
        val ingredients = mutableListOf<ParsedIngredient>()
        val steps = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        for (line in lines) {
            if (line.isBlank()) continue

            val titleMatch = TITLE.find(line)
            if (titleMatch != null) {
                if (title == null) title = titleMatch.groupValues[1].trim()
                section = Section.NONE
                continue
            }

            val heading = HEADING.find(line)
            if (heading != null) {
                section = classify(heading.groupValues[2])
                continue
            }

            when (section) {
                Section.INGREDIENTS -> {
                    val body = BULLET.find(line)?.groupValues?.get(1) ?: line.trim()
                    val parsed = parseIngredient(body)
                    if (parsed == null) {
                        warnings.add("Ligne d'ingrédient ignorée : « ${body.trim()} »")
                    } else {
                        ingredients.add(parsed)
                    }
                }

                Section.STEPS -> {
                    val body = NUMBERED.find(line)?.groupValues?.get(1)
                        ?: BULLET.find(line)?.groupValues?.get(1)
                        ?: line.trim()
                    if (body.isNotBlank()) steps.add(body.trim())
                }

                Section.NONE, Section.OTHER -> Unit
            }
        }

        if (ingredients.isEmpty()) {
            warnings.add("Aucun ingrédient trouvé : vérifiez la présence d'une section « ## Ingrédients ».")
        }

        return ParsedRecipe(
            title = title?.takeIf { it.isNotBlank() } ?: fallbackTitle,
            ingredients = ingredients,
            steps = steps,
            warnings = warnings,
        )
    }

    private fun classify(heading: String): Section {
        val plain = heading.deaccent().lowercase()
        return when {
            plain.contains("ingr") -> Section.INGREDIENTS
            plain.contains("etape") || plain.contains("preparation") ||
                plain.contains("instruction") || plain.contains("realisation") -> Section.STEPS
            else -> Section.OTHER
        }
    }

    /** Transforme « 100 g de farine » en [ParsedIngredient]. Renvoie null si la ligne est vide. */
    fun parseIngredient(rawLine: String): ParsedIngredient? {
        val cleaned = OPTIONAL.replace(rawLine.trim(), "").trim()
        if (cleaned.isBlank()) return null

        val quantityMatch = QUANTITY.find(cleaned)
        if (quantityMatch == null) {
            // Pas de quantité : « sel », « poivre du moulin ».
            val name = cleanName(stripConnector(cleaned))
            return if (name.isBlank()) null else ParsedIngredient(
                name = name,
                quantity = 1.0,
                unit = IngredientUnit.PIECE,
                unspecified = true,
                rawLine = rawLine.trim(),
            )
        }

        val quantity = parseQuantity(quantityMatch.groupValues[1])
        val afterQuantity = cleaned.substring(quantityMatch.value.length)
        val unitMatch = UnitVocabulary.match(afterQuantity)

        val rest = if (unitMatch != null) afterQuantity.substring(unitMatch.matchedLength) else afterQuantity
        val name = cleanName(stripConnector(rest.trimStart(' ', '.', ',', ';', ':')))

        if (name.isBlank()) {
            // « 3 oeufs » où le nom serait absorbé par l'unité : on garde le texte brut.
            val fallback = cleanName(afterQuantity)
            if (fallback.isBlank()) return null
            return ParsedIngredient(
                name = fallback,
                quantity = quantity,
                unit = IngredientUnit.PIECE,
                rawLine = rawLine.trim(),
            )
        }

        return if (unitMatch == null) {
            ParsedIngredient(
                name = name,
                quantity = quantity,
                unit = IngredientUnit.PIECE,
                rawLine = rawLine.trim(),
            )
        } else {
            ParsedIngredient(
                name = name,
                quantity = quantity * unitMatch.factor,
                unit = unitMatch.unit,
                freeUnitLabel = unitMatch.freeLabel,
                rawLine = rawLine.trim(),
            )
        }
    }

    /** `2`, `1.5`, `1,5`, `1/2`, `1 1/2` → Double. */
    internal fun parseQuantity(text: String): Double {
        val t = text.trim().replace(",", ".")
        val mixed = Regex("""^(\d+)\s+(\d+)\s*/\s*(\d+)$""").find(t)
        if (mixed != null) {
            val (whole, num, den) = mixed.destructured
            val d = den.toDouble()
            return whole.toDouble() + if (d == 0.0) 0.0 else num.toDouble() / d
        }
        val fraction = Regex("""^(\d+)\s*/\s*(\d+)$""").find(t)
        if (fraction != null) {
            val (num, den) = fraction.destructured
            val d = den.toDouble()
            return if (d == 0.0) 0.0 else num.toDouble() / d
        }
        return t.toDoubleOrNull() ?: 1.0
    }

    private fun stripConnector(text: String): String = CONNECTOR.replace(text.trimStart(), "")

    /** Retire le balisage markdown résiduel (gras, italique, liens) et les espaces multiples. */
    private fun cleanName(text: String): String = text
        .replace(Regex("""\*\*|__|\*|_|`"""), "")
        .replace(Regex("""\[([^\]]*)\]\([^)]*\)"""), "$1")
        .replace(Regex("""\s+"""), " ")
        .trim()
        .trim('-', ',', ';', ':', '.')
        .trim()
}
