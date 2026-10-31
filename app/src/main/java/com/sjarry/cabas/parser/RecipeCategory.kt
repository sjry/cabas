package com.sjarry.cabas.parser

import java.util.Locale

/**
 * Type de plat d'une recette, lu dans la ligne « Catégorie : … » du markdown.
 *
 * À ne pas confondre avec [IngredientCategory], le rayon du magasin où se trouve un
 * ingrédient. L'ordre de déclaration est l'ordre du repas : c'est celui des puces de filtre.
 */
enum class RecipeCategory(val label: String, private val aliases: List<String>) {
    APERITIF("Apéritif", listOf("aperitif", "apero")),
    STARTER("Entrée", listOf("entree")),
    MAIN("Plat", listOf("plat", "plat principal", "principal", "plat de resistance")),
    SIDE("Accompagnement", listOf("accompagnement", "garniture")),
    DESSERT("Dessert", listOf("dessert")),
    SNACK("Goûter", listOf("gouter", "petit dejeuner", "petit dej", "collation")),
    DRINK("Boisson", listOf("boisson", "cocktail")),
    SAUCE("Sauce", listOf("sauce", "condiment"));

    companion object {
        /**
         * Reconnaît une valeur écrite à la main : casse, accents, traits d'union, guillemets
         * (YAML en met parfois) et pluriel sont indifférents. Null si la valeur est inconnue.
         *
         * Égalité exacte sur les alias, jamais `contains` : « plateau de fromages » n'est pas
         * un plat.
         */
        fun parse(text: String): RecipeCategory? {
            val plain = text.normalizeMetadataValue()
            if (plain.isEmpty()) return null
            return byAlias(plain) ?: plain.removeSuffix("s").takeIf { it != plain }?.let(::byAlias)
        }

        private fun byAlias(plain: String): RecipeCategory? =
            entries.firstOrNull { plain in it.aliases }

        /** Les catégories effectivement rencontrées, sans doublon, dans l'ordre du repas. */
        fun present(values: Iterable<RecipeCategory?>): List<RecipeCategory> {
            val seen = values.filterNotNull().toSet()
            return entries.filter { it in seen }
        }
    }
}

private val SPACES = Regex("""\s+""")

/**
 * Met une valeur de métadonnée (« Catégorie : … », « Saison : … ») sous forme comparable aux
 * alias : sans accent, en minuscules, traits d'union en espaces, guillemets retirés (YAML en
 * met parfois), espaces réduits.
 */
internal fun String.normalizeMetadataValue(): String = deaccent()
    .lowercase(Locale.ROOT)
    .replace('-', ' ')
    .replace('’', '\'')
    .trim('"', '\'', ' ')
    .replace(SPACES, " ")
    .trim()
