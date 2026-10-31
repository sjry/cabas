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

/** Une section du fichier que le parseur ne comprend pas (« Notes », « Astuces »…), conservée telle quelle. */
data class ParsedSection(val title: String, val lines: List<String>)

/** Résultat du parsing d'un fichier markdown. */
data class ParsedRecipe(
    val title: String,
    val ingredients: List<ParsedIngredient>,
    /** Type de plat, absent si le fichier ne le dit pas. */
    val category: RecipeCategory? = null,
    /** Saisons de la recette, vide si le fichier ne le dit pas. */
    val seasons: Set<Season> = emptySet(),
    val steps: List<String>,
    /** Anomalies non bloquantes à afficher après l'import. */
    val warnings: List<String>,
    /** Les autres sections, dans l'ordre du fichier, pour le détail de la recette. */
    val notes: List<ParsedSection> = emptyList(),
)

/**
 * Parseur du format de recette maison : un titre `#`, une section `## Ingrédients`
 * et une section `## Étapes`, avec en option des lignes `Catégorie : …` et `Saison : …` avant
 * la première section. Toute autre section (« Notes », « Astuces »…) est conservée avec son
 * titre, pour le détail. Volontairement tolérant — une ligne mal formée est conservée telle
 * quelle et signalée plutôt que perdue.
 */
object RecipeParser {

    private val TITLE = Regex("""^\s{0,3}#\s+(.*\S)\s*$""")
    private val HEADING = Regex("""^\s{0,3}(#{2,6})\s+(.*\S)\s*$""")
    private val BULLET = Regex("""^\s*[-*+]\s+(.*\S)\s*$""")
    private val NUMBERED = Regex("""^\s*\d+\s*[.)]\s+(.*\S)\s*$""")

    /**
     * Ligne `clé : valeur` du préambule, valeur non vide. Une puce ou un titre n'en est pas une,
     * ni les `---` d'un front matter YAML (Obsidian), qui n'ont pas de `:` : ils sont ignorés.
     */
    private val METADATA = Regex("""^\s{0,3}([^:#\-*+][^:]*?)\s*:\s*(.*\S)\s*$""")

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
        var category: RecipeCategory? = null
        var categorySeen = false
        var seasons: Set<Season> = emptySet()
        var seasonsSeen = false
        var section = Section.NONE
        val ingredients = mutableListOf<ParsedIngredient>()
        val steps = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val notes = mutableListOf<ParsedSection>()
        var noteLines: MutableList<String>? = null

        for (line in lines) {
            if (line.isBlank()) {
                // Dans une section conservée, une ligne vide est un saut de paragraphe ; ailleurs
                // elle ne dit rien.
                noteLines?.add("")
                continue
            }

            val titleMatch = TITLE.find(line)
            if (titleMatch != null) {
                if (title == null) title = titleMatch.groupValues[1].trim()
                section = Section.NONE
                continue
            }

            val heading = HEADING.find(line)
            if (heading != null) {
                val text = heading.groupValues[2].trim()
                section = classify(text)
                // Une section inconnue est gardée avec son titre ; un sous-titre `###` à
                // l'intérieur ouvre simplement une section de plus, c'est assumé.
                noteLines = if (section == Section.OTHER) {
                    mutableListOf<String>().also { notes.add(ParsedSection(text, it)) }
                } else {
                    null
                }
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

                // Le préambule — avant le titre pour un front matter, entre le titre et la
                // première section sinon — est le seul endroit où une métadonnée est lue.
                // Toute autre ligne y est ignorée en silence, comme avant : le format n'a
                // jamais interdit une phrase d'introduction.
                // Seule la première occurrence d'une clé compte, comme pour le titre.
                Section.NONE -> {
                    val meta = METADATA.find(line) ?: continue
                    val key = meta.groupValues[1].deaccent().lowercase().trim()
                    val value = meta.groupValues[2].trim()
                    when (key) {
                        "categorie", "category" -> if (!categorySeen) {
                            categorySeen = true
                            category = RecipeCategory.parse(value)
                            if (category == null) warnings.add("Catégorie inconnue : « $value »")
                        }

                        "saison", "saisons", "season" -> if (!seasonsSeen) {
                            seasonsSeen = true
                            val (known, unknown) = Season.parseList(value)
                            seasons = known
                            unknown.forEach { warnings.add("Saison inconnue : « $it »") }
                        }
                    }
                }

                Section.OTHER -> {
                    val bullet = BULLET.find(line)?.groupValues?.get(1)
                    noteLines?.add(if (bullet != null) "• ${bullet.trim()}" else line.trim())
                }
            }
        }

        if (ingredients.isEmpty()) {
            warnings.add("Aucun ingrédient trouvé : vérifiez la présence d'une section « ## Ingrédients ».")
        }

        return ParsedRecipe(
            notes = notes
                .map { ParsedSection(it.title, it.lines.dropWhile { l -> l.isEmpty() }.dropLastWhile { l -> l.isEmpty() }) }
                .filter { it.lines.isNotEmpty() },
            title = title?.takeIf { it.isNotBlank() } ?: fallbackTitle,
            ingredients = ingredients,
            category = category,
            seasons = seasons,
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
