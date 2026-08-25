package com.sjarry.cabas.data

import kotlin.random.Random

/**
 * Tirage au sort des recettes à ajouter au menu, pour les soirs où l'on ne sait pas quoi
 * cuisiner.
 *
 * L'exclusion de ce qui est déjà au menu se fait ici, en Kotlin, et non dans la requête :
 * c'est la règle du tirage, elle doit être couverte par les tests JVM. Le [Random] est un
 * paramètre pour la même raison — c'est le seul moyen de rejouer un tirage.
 */
object MenuDraw {

    /** Nombre proposé par défaut dans le dialogue : de quoi remplir quelques repas. */
    const val DEFAULT_COUNT = 3

    /**
     * Tire au sort [count] recettes parmi celles qui ne sont pas déjà au menu. Rend moins
     * d'identifiants que demandé quand la bibliothèque ne suffit pas — jamais de doublon,
     * jamais une recette déjà au menu.
     */
    fun draw(
        allRecipeIds: List<Long>,
        inMenu: Set<Long>,
        count: Int,
        random: Random,
    ): List<Long> = allRecipeIds
        .filterNot { it in inMenu }
        .shuffled(random)
        .take(count.coerceAtLeast(0))
}
