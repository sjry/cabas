package com.sjarry.cabas.data

import com.sjarry.cabas.parser.deaccent
import java.text.Collator
import java.util.Locale

/** Une recette candidate à l'ajout au menu, avec ses ingrédients aplatis pour la recherche. */
data class RecipeCandidate(
    val id: Long,
    val title: String,
    /** Noms des ingrédients joints par [RecipeSearch.SEPARATOR], tels que les rend `GROUP_CONCAT`. */
    val ingredientNames: String,
)

/**
 * Les noms d'ingrédients dépaquetés. Une recette sans ingrédient a une chaîne vide, qu'un
 * `split` naïf transformerait en un ingrédient au nom vide.
 */
fun RecipeCandidate.ingredients(): List<String> =
    if (ingredientNames.isEmpty()) emptyList() else ingredientNames.split(RecipeSearch.SEPARATOR)

/**
 * Prépare la liste du sélecteur de recettes : écarte ce qui est déjà au menu, filtre sur la
 * saisie — titre **et** ingrédients — puis jalonne le résultat.
 *
 * Sans recherche, la liste est découpée par initiale ; avec une recherche, elle est classée par
 * pertinence et n'a plus d'initiales à afficher.
 *
 * Logique pure, sans dépendance Android, pour rester testable en JVM.
 */
object RecipeSearch {

    /**
     * Séparateur des noms d'ingrédients : U+001F, qu'aucun nom ne peut contenir — contrairement à
     * `|` ou `,`. Son pendant côté SQL est `char(31)`, dans `RecipeDao.observeCandidates`.
     */
    const val SEPARATOR = "\u001F"

    /** En-tête des titres qui ne commencent pas par une lettre : « 3 chocolats », « 100 % cacao ». */
    const val OTHER_HEADER = "#"

    /**
     * Tri français : à `PRIMARY`, « Éclair » se range entre « E » et « F ». Le tri SQL ne sait pas
     * le faire — `COLLATE NOCASE` ne couvre que l'ASCII et renvoie les accents après « Z ».
     */
    private val collator: Collator = Collator.getInstance(Locale.FRANCE).apply {
        strength = Collator.PRIMARY
    }

    data class Match(
        val id: Long,
        val title: String,
        val ingredientCount: Int,
        /**
         * Ingrédient qui a fait remonter la recette, à afficher en sous-titre pour expliquer sa
         * présence. Null quand le titre suffisait.
         */
        val matchedIngredient: String? = null,
    )

    /** Une tranche de la liste. [header] est null pendant une recherche : plus d'alphabet à jalonner. */
    data class Section(val header: String?, val matches: List<Match>)

    fun sections(
        candidates: List<RecipeCandidate>,
        inMenu: Set<Long>,
        query: String,
    ): List<Section> {
        val available = candidates.filterNot { it.id in inMenu }
        val tokens = tokenize(query)
        return if (tokens.isEmpty()) grouped(available) else ranked(available, tokens)
    }

    /** Sans recherche : une section par initiale, dans l'ordre, « # » en dernier. */
    private fun grouped(available: List<RecipeCandidate>): List<Section> {
        val byInitial = available
            .map { Match(it.id, it.title, it.ingredients().size) }
            .sortedWith { a, b -> collator.compare(a.title, b.title) }
            .groupBy { initialOf(it.title) }

        // La liste étant déjà triée, `groupBy` livre les initiales dans l'ordre ; seul « # »
        // doit être déplacé, le collator plaçant les chiffres avant les lettres.
        return byInitial.entries
            .filter { it.key != OTHER_HEADER }
            .map { Section(it.key, it.value) } +
            (byInitial[OTHER_HEADER]?.let { listOf(Section(OTHER_HEADER, it)) } ?: emptyList())
    }

    /**
     * Avec une recherche : une seule section, classée en trois rangs — titre qui commence par la
     * saisie, titre qui la contient, puis correspondance venue des ingrédients. Une liste vide
     * signifie « aucun résultat ».
     */
    private fun ranked(available: List<RecipeCandidate>, tokens: List<String>): List<Section> {
        val scored = available.mapNotNull { candidate ->
            val title = ShoppingListBuilder.normalizeName(candidate.title)
            val ingredients = candidate.ingredients()
            val normalized = ingredients.map { ShoppingListBuilder.normalizeName(it) }

            // Chaque jeton doit se trouver quelque part : « curry poulet » ne remonte que ce qui
            // porte les deux, dans le titre ou dans les ingrédients.
            var fromIngredient: String? = null
            for (token in tokens) {
                if (title.contains(token)) continue
                val index = normalized.indexOfFirst { it.contains(token) }
                if (index < 0) return@mapNotNull null
                if (fromIngredient == null) fromIngredient = ingredients[index]
            }

            val rank = when {
                fromIngredient != null -> 2
                title.startsWith(tokens.first()) -> 0
                else -> 1
            }
            rank to Match(candidate.id, candidate.title, ingredients.size, fromIngredient)
        }

        if (scored.isEmpty()) return emptyList()

        val sorted = scored
            .sortedWith(
                compareBy<Pair<Int, Match>> { it.first }
                    .thenComparator { a, b -> collator.compare(a.second.title, b.second.title) },
            )
            .map { it.second }
        return listOf(Section(header = null, matches = sorted))
    }

    /** « Éclair au café » → « E », « 3 chocolats » → « # ». */
    private fun initialOf(title: String): String {
        val first = title.trimStart().take(1).deaccent().uppercase(Locale.FRANCE)
        return if (first.length == 1 && first[0] in 'A'..'Z') first else OTHER_HEADER
    }

    /**
     * Découpe la saisie en jetons normalisés. `normalizeName` réduit déjà les espaces et retire
     * les accents : c'est le même normaliseur que la liste de courses, il n'y en a pas deux.
     */
    private fun tokenize(query: String): List<String> =
        ShoppingListBuilder.normalizeName(query).split(' ').filter { it.isNotEmpty() }
}
