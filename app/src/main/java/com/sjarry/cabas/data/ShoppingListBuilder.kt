package com.sjarry.cabas.data

import com.sjarry.cabas.data.model.MenuRecipe
import com.sjarry.cabas.data.model.RecipeSection
import com.sjarry.cabas.data.model.ShoppingItem
import com.sjarry.cabas.data.model.ShoppingList
import com.sjarry.cabas.parser.CategoryVocabulary
import com.sjarry.cabas.parser.IngredientCategory
import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.ParsedIngredient
import com.sjarry.cabas.parser.deaccent
import java.text.Collator
import java.util.Locale

/**
 * Construit la liste de courses à partir du menu : chaque ingrédient est
 * multiplié par le nombre de convives de sa recette, puis les ingrédients
 * identiques sont regroupés pour la vue « Total », et chaque article reçoit son
 * rayon de magasin.
 *
 * Logique pure, sans dépendance Android, pour rester testable en JVM.
 */
object ShoppingListBuilder {

    private val collator: Collator = Collator.getInstance(Locale.FRANCE).apply {
        strength = Collator.PRIMARY
    }

    fun build(
        menu: List<MenuRecipe>,
        checkedKeys: Set<String> = emptySet(),
        categoryOverrides: Map<String, IngredientCategory> = emptyMap(),
    ): ShoppingList =
        ShoppingList(
            total = aggregate(menu, checkedKeys, categoryOverrides),
            sections = sections(menu, checkedKeys, categoryOverrides),
        )

    /** Vue « Total » : ingrédients fusionnés, triés alphabétiquement. */
    fun aggregate(
        menu: List<MenuRecipe>,
        checkedKeys: Set<String> = emptySet(),
        categoryOverrides: Map<String, IngredientCategory> = emptyMap(),
    ): List<ShoppingItem> {
        val merged = LinkedHashMap<String, ShoppingItem>()

        for (recipe in menu) {
            for (ingredient in recipe.ingredients) {
                val key = keyOf(ingredient)
                val scaled = ingredient.quantity * recipe.servings
                val existing = merged[key]
                merged[key] = if (existing == null) {
                    ShoppingItem(
                        key = key,
                        name = ingredient.name,
                        quantity = scaled,
                        unit = ingredient.unit,
                        freeUnitLabel = ingredient.freeUnitLabel,
                        unspecified = ingredient.unspecified,
                        checked = key in checkedKeys,
                        category = categoryOf(ingredient.name, categoryOverrides),
                    )
                } else {
                    existing.copy(quantity = existing.quantity + scaled)
                }
            }
        }

        // « sel » sans quantité est redondant si une autre recette en chiffre déjà :
        // on ne garde alors que la ligne chiffrée.
        val quantified = merged.values
            .filterNot { it.unspecified }
            .map { normalizeName(it.name) }
            .toSet()

        return merged.values
            .filterNot { it.unspecified && normalizeName(it.name) in quantified }
            .sortedWith(Comparator { a, b -> collator.compare(a.name, b.name) })
    }

    /** Vue « Par recette » : quantités multipliées, groupées par recette. */
    fun sections(
        menu: List<MenuRecipe>,
        checkedKeys: Set<String> = emptySet(),
        categoryOverrides: Map<String, IngredientCategory> = emptyMap(),
    ): List<RecipeSection> =
        menu.map { recipe ->
            RecipeSection(
                recipeId = recipe.recipeId,
                title = recipe.title,
                servings = recipe.servings,
                items = recipe.ingredients.map { ingredient ->
                    val key = keyOf(ingredient)
                    ShoppingItem(
                        key = key,
                        name = ingredient.name,
                        quantity = ingredient.quantity * recipe.servings,
                        unit = ingredient.unit,
                        freeUnitLabel = ingredient.freeUnitLabel,
                        unspecified = ingredient.unspecified,
                        checked = key in checkedKeys,
                        category = categoryOf(ingredient.name, categoryOverrides),
                    )
                },
            )
        }

    /**
     * Rayon d'un article : la correction apprise l'emporte sur le lexique. La
     * correction est retenue sous le **nom normalisé**, donc elle vaut pour toutes
     * les recettes et toutes les unités — comme la case à cocher.
     */
    fun categoryOf(
        name: String,
        categoryOverrides: Map<String, IngredientCategory> = emptyMap(),
    ): IngredientCategory =
        categoryOverrides[normalizeName(name)] ?: CategoryVocabulary.categorize(name)

    /**
     * Clé de regroupement : nom normalisé + unité. Une masse et un volume du même
     * ingrédient restent deux lignes distinctes — on ne peut pas les additionner.
     *
     * **Le rayon n'entre pas dans la clé** : corriger un rayon ne doit pas décocher
     * l'article ni le dédoubler.
     */
    fun keyOf(ingredient: ParsedIngredient): String {
        val name = normalizeName(ingredient.name)
        val unit = when {
            ingredient.unspecified -> "qs"
            ingredient.unit == IngredientUnit.FREE -> "free:${ingredient.freeUnitLabel.orEmpty().lowercase()}"
            else -> ingredient.unit.name
        }
        return "$name|$unit"
    }

    /** « Farine  T55 » et « farine t55 » désignent le même article. */
    fun normalizeName(name: String): String = name
        .deaccent()
        .lowercase(Locale.FRANCE)
        .replace(Regex("""\s+"""), " ")
        .trim()
}
