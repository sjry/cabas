package com.sjarry.cabas.parser

import java.time.LocalDate
import java.time.MonthDay

/**
 * Saison d'une recette, lue dans la ligne « Saison : … » du markdown. Une recette peut en
 * porter plusieurs (« automne, hiver »). L'ordre de déclaration est celui de l'année : c'est
 * celui des puces de filtre et du libellé.
 */
enum class Season(val label: String, private val aliases: List<String>) {
    SPRING("Printemps", listOf("printemps")),
    SUMMER("Été", listOf("ete")),
    AUTUMN("Automne", listOf("automne")),
    WINTER("Hiver", listOf("hiver"));

    companion object {
        /** « toutes », « toute l'année », « toutes saisons »… : les quatre d'un coup. */
        private val ALL_ALIASES = listOf(
            "toute", "toutes", "toute saison", "toutes saisons", "toutes les saisons",
            "toute l'annee", "toute annee", "annee", "all",
        )

        /** Séparateurs d'une liste écrite à la main : « automne, hiver », « été et printemps », « été/automne ». */
        private val LIST_SEPARATOR = Regex("""[,;/&+]|\s+et\s+""")

        /**
         * Reconnaît une saison écrite à la main, avec la même tolérance que [RecipeCategory.parse].
         * Rend un ensemble parce qu'une valeur peut en désigner plusieurs (« toutes ») ; null si
         * la valeur est inconnue.
         */
        fun parse(text: String): Set<Season>? {
            val plain = text.normalizeMetadataValue()
            if (plain.isEmpty()) return null
            if (plain in ALL_ALIASES) return entries.toSet()
            val one = entries.firstOrNull { plain in it.aliases }
                ?: plain.removeSuffix("s").takeIf { it != plain }?.let { s -> entries.firstOrNull { s in it.aliases } }
            return one?.let { setOf(it) }
        }

        /**
         * Lit une liste : chaque morceau passe par [parse]. Les morceaux inconnus sont rendus
         * tels quels, à signaler, sans faire perdre ceux qui ont été compris.
         */
        fun parseList(text: String): Pair<Set<Season>, List<String>> {
            val known = mutableSetOf<Season>()
            val unknown = mutableListOf<String>()
            text.split(LIST_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }.forEach { piece ->
                val parsed = parse(piece)
                if (parsed == null) unknown.add(piece) else known.addAll(parsed)
            }
            return known to unknown
        }

        /**
         * La saison d'une date, aux bornes usuelles du calendrier : printemps dès le 20 mars,
         * été dès le 21 juin, automne dès le 22 septembre, hiver dès le 21 décembre. Les dates
         * exactes des équinoxes varient d'un jour selon l'année ; en cuisine, le jour près ne
         * compte pas.
         */
        fun of(date: LocalDate): Season {
            val day = MonthDay.from(date)
            return when {
                day < MonthDay.of(3, 20) -> WINTER
                day < MonthDay.of(6, 21) -> SPRING
                day < MonthDay.of(9, 22) -> SUMMER
                day < MonthDay.of(12, 21) -> AUTUMN
                else -> WINTER
            }
        }

        /** La saison du jour, seule source à utiliser pour une présélection. */
        fun current(): Season = of(LocalDate.now())

        /** Les saisons effectivement rencontrées, sans doublon, dans l'ordre de l'année. */
        fun present(values: Iterable<Set<Season>>): List<Season> {
            val seen = values.flatten().toSet()
            return entries.filter { it in seen }
        }

        /**
         * « Été, automne » — la première capitalisée, les suivantes en minuscules, dans l'ordre de
         * l'année. « Toutes saisons » quand les quatre y sont, chaîne vide sans saison.
         */
        fun label(seasons: Set<Season>): String {
            if (seasons.isEmpty()) return ""
            if (seasons.size == entries.size) return "Toutes saisons"
            return entries.filter { it in seasons }
                .mapIndexed { index, season -> if (index == 0) season.label else season.label.lowercase() }
                .joinToString(", ")
        }
    }
}
