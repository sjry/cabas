package com.sjarry.cabas.parser

/**
 * Famille d'unité. Deux ingrédients ne peuvent être additionnés que s'ils
 * partagent la même famille (et, pour [UnitFamily.FREE], le même libellé).
 */
enum class UnitFamily { MASS, VOLUME, PIECE, FREE }

/**
 * Unité canonique dans laquelle les quantités sont stockées :
 * grammes pour les masses, millilitres pour les volumes.
 */
enum class IngredientUnit(val family: UnitFamily) {
    GRAM(UnitFamily.MASS),
    MILLILITER(UnitFamily.VOLUME),
    PIECE(UnitFamily.PIECE),
    FREE(UnitFamily.FREE),
}

/** Une unité reconnue dans le markdown, avec son facteur vers l'unité canonique. */
internal data class UnitMatch(
    val unit: IngredientUnit,
    val factor: Double,
    val freeLabel: String?,
    val matchedLength: Int,
)

internal object UnitVocabulary {

    private data class Rule(
        val regex: Regex,
        val unit: IngredientUnit,
        val factor: Double,
        val freeLabel: String?,
    )

    /** Compile un motif ancré au début, insensible à la casse, suivi d'une frontière. */
    private fun p(body: String) = Regex("""^(?:$body)(?=[\s.,;:]|$)""", RegexOption.IGNORE_CASE)

    private fun mass(body: String, factor: Double) = Rule(p(body), IngredientUnit.GRAM, factor, null)
    private fun vol(body: String, factor: Double) = Rule(p(body), IngredientUnit.MILLILITER, factor, null)
    private fun free(body: String, label: String) = Rule(p(body), IngredientUnit.FREE, 1.0, label)

    /**
     * Motifs testés au début du texte qui suit la quantité. Le plus long match
     * l'emporte, ce qui évite que « cl » soit reconnu au lieu de « cl. à café ».
     */
    private val rules: List<Rule> = listOf(
        mass("kg|kilogrammes?|kilos?", 1000.0),
        mass("g|gr|grammes?", 1.0),
        vol("l|litres?", 1000.0),
        vol("dl|decilitres?", 100.0),
        vol("cl|centilitres?", 10.0),
        vol("ml|millilitres?", 1.0),
        // « cas » / « cac » sont les libellés canoniques ; les écritures longues
        // (« c. à soupe », « c.a.s. », « cuillère à soupe ») restent acceptées à la lecture.
        free("""(?:c|cuilleres?)\.?[\s.]*a[\s.]*(?:s|soupe)\.?""", "cas"),
        free("""(?:c|cuilleres?)\.?[\s.]*a[\s.]*(?:c|cafe)\.?""", "cac"),
        free("cuilleres?", "cuillère"),
        free("pincees?", "pincée"),
        free("gousses?", "gousse"),
        free("sachets?", "sachet"),
        free("bottes?", "botte"),
        free("brins?", "brin"),
        free("branches?", "branche"),
        free("feuilles?", "feuille"),
        free("tranches?", "tranche"),
        free("boites?", "boîte"),
        free("verres?", "verre"),
        free("paquets?", "paquet"),
        free("bouquets?", "bouquet"),
        free("poignees?", "poignée"),
    )

    /**
     * Cherche une unité au début de [text]. La comparaison se fait sur une version
     * sans accents pour accepter « décilitre » comme « decilitre » ; la
     * désaccentuation est caractère à caractère, les longueurs sont donc
     * directement réutilisables sur le texte d'origine.
     */
    fun match(text: String): UnitMatch? {
        val plain = text.deaccent()
        var best: UnitMatch? = null
        for (rule in rules) {
            val found = rule.regex.find(plain) ?: continue
            val length = found.value.length
            if (best == null || length > best.matchedLength) {
                best = UnitMatch(rule.unit, rule.factor, rule.freeLabel, length)
            }
        }
        return best
    }
}

/** Retire les accents sans changer le nombre de caractères. */
internal fun String.deaccent(): String = buildString(length) {
    for (c in this@deaccent) {
        val i = ACCENTED.indexOf(c)
        append(if (i >= 0) PLAIN[i] else c)
    }
}

private const val ACCENTED = "àáâãäåÀÁÂÃÄÅèéêëÈÉÊËìíîïÌÍÎÏòóôõöÒÓÔÕÖùúûüÙÚÛÜçÇñÑÿŸœŒæÆ"
private const val PLAIN = "aaaaaaAAAAAAeeeeEEEEiiiiIIIIoooooOOOOOuuuuUUUUcCnNyYoOaA"
