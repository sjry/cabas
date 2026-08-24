package com.sjarry.cabas.data.model

import com.sjarry.cabas.parser.IngredientCategory
import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.ParsedIngredient

/** Une recette du menu avec son nombre de convives. */
data class MenuRecipe(
    val recipeId: Long,
    val title: String,
    val servings: Int,
    /** Ingrédients pour 1 personne. */
    val ingredients: List<ParsedIngredient>,
    /** Recette déjà cuisinée. N'influe pas sur la liste de courses. */
    val done: Boolean = false,
)

/** Une ligne de la liste de courses. */
data class ShoppingItem(
    /** Clé stable partagée par les trois vues : c'est elle qui porte la case à cocher. */
    val key: String,
    val name: String,
    val quantity: Double,
    val unit: IngredientUnit,
    val freeUnitLabel: String? = null,
    val unspecified: Boolean = false,
    val checked: Boolean = false,
    /** Rayon de magasin, déduit du nom ou corrigé par l'utilisateur. */
    val category: IngredientCategory = IngredientCategory.OTHER,
)

/** Bloc « un rayon » de la vue magasin. */
data class AisleSection(
    val category: IngredientCategory,
    val items: List<ShoppingItem>,
) {
    /** Ce qu'il reste à prendre dans ce rayon : le seul contenu affiché en magasin. */
    val remaining: List<ShoppingItem> get() = items.filterNot { it.checked }
}

/** Bloc « une recette » de la vue détaillée. */
data class RecipeSection(
    val recipeId: Long,
    val title: String,
    val servings: Int,
    val items: List<ShoppingItem>,
) {
    /** Ce qu'il reste à prendre pour cette recette : le seul contenu affiché en magasin. */
    val remaining: List<ShoppingItem> get() = items.filterNot { it.checked }
}

/** Liste de courses complète, dans ses trois présentations. */
data class ShoppingList(
    val total: List<ShoppingItem>,
    val sections: List<RecipeSection>,
) {
    /**
     * Vue « Rayon » : le même contenu que [total], regroupé par rayon dans l'ordre du
     * parcours en magasin. Dérivé plutôt que stocké, pour ne pas pouvoir se
     * désynchroniser de [total] ; l'ordre alphabétique à l'intérieur d'un rayon vient
     * gratuitement du tri déjà appliqué à [total].
     */
    val aisles: List<AisleSection> get() {
        val byCategory = total.groupBy { it.category }
        return IngredientCategory.entries.mapNotNull { category ->
            byCategory[category]?.let { AisleSection(category, it) }
        }
    }

    val itemCount: Int get() = total.size
    val checkedCount: Int get() = total.count { it.checked }
    val isEmpty: Boolean get() = total.isEmpty()

    /** Articles restant à prendre, dans l'ordre alphabétique de la vue « Total ». */
    val remaining: List<ShoppingItem> get() = total.filterNot { it.checked }

    /**
     * Articles déjà pris, relégués dans la section repliable en bas de liste.
     * Toujours à plat et alphabétiques, quelle que soit la vue : c'est « ce qui
     * est déjà dans le cabas », pas un reflet du menu.
     */
    val taken: List<ShoppingItem> get() = total.filter { it.checked }

    /** Une liste vide n'est pas une liste terminée : rien n'a été acheté. */
    val isComplete: Boolean get() = total.isNotEmpty() && total.all { it.checked }
}
