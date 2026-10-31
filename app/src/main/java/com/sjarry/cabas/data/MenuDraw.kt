package com.sjarry.cabas.data

import com.sjarry.cabas.parser.RecipeCategory
import com.sjarry.cabas.parser.Season
import kotlin.random.Random

/** Ce que le tirage au sort a besoin de connaître d'une recette. Projection Room de `recipes`. */
data class DrawCandidate(
    val id: Long,
    val category: RecipeCategory? = null,
    val seasons: Set<Season> = emptySet(),
)

/**
 * Tirage au sort des recettes à ajouter au menu, pour les soirs où l'on ne sait pas quoi
 * cuisiner.
 *
 * L'exclusion de ce qui est déjà au menu et les contraintes de catégorie et de saison se font
 * ici, en Kotlin, et non dans la requête : c'est la règle du tirage, elle doit être couverte
 * par les tests JVM. Le [Random] est un paramètre pour la même raison — c'est le seul moyen de
 * rejouer un tirage.
 */
object MenuDraw {

    /** Nombre proposé par défaut dans le dialogue : de quoi remplir quelques repas. */
    const val DEFAULT_COUNT = 3

    /**
     * Les candidates hors menu qui respectent les contraintes : [category] si elle est donnée,
     * et **au moins une** saison parmi [seasons] si l'ensemble n'est pas vide.
     *
     * Un ensemble vide veut dire « toutes les saisons », recettes sans saison comprises — ce
     * n'est pas « les recettes marquées toutes saisons ». Et « au moins une en commun » plutôt
     * qu'une égalité d'ensembles : c'est ce qui permet au dé d'une carte de remplacer un plat
     * automne-hiver par un plat d'automne, et qui rend une recette « toutes saisons » (les
     * quatre) toujours éligible dès qu'une saison est demandée.
     */
    fun eligible(
        candidates: List<DrawCandidate>,
        inMenu: Set<Long>,
        category: RecipeCategory? = null,
        seasons: Set<Season> = emptySet(),
    ): List<DrawCandidate> = candidates
        .filterNot { it.id in inMenu }
        .filter { category == null || it.category == category }
        .filter { seasons.isEmpty() || it.seasons.any { season -> season in seasons } }

    /**
     * Tire au sort [count] recettes parmi les [eligible]. Rend moins d'identifiants que demandé
     * quand la bibliothèque ne suffit pas — jamais de doublon, jamais une recette déjà au menu.
     */
    fun draw(
        candidates: List<DrawCandidate>,
        inMenu: Set<Long>,
        count: Int,
        random: Random,
        category: RecipeCategory? = null,
        seasons: Set<Season> = emptySet(),
    ): List<Long> = eligible(candidates, inMenu, category, seasons)
        .map { it.id }
        .shuffled(random)
        .take(count.coerceAtLeast(0))
}
