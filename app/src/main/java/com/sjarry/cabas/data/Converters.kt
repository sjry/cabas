package com.sjarry.cabas.data

import androidx.room.TypeConverter
import com.sjarry.cabas.parser.IngredientCategory
import com.sjarry.cabas.parser.IngredientUnit
import com.sjarry.cabas.parser.RecipeCategory
import com.sjarry.cabas.parser.Season

class Converters {
    @TypeConverter
    fun unitToString(unit: IngredientUnit): String = unit.name

    @TypeConverter
    fun stringToUnit(value: String): IngredientUnit =
        runCatching { IngredientUnit.valueOf(value) }.getOrDefault(IngredientUnit.PIECE)

    @TypeConverter
    fun categoryToString(category: IngredientCategory): String = category.name

    /** Un rayon disparu d'une version future retombe dans « Divers », sans planter. */
    @TypeConverter
    fun stringToCategory(value: String): IngredientCategory =
        runCatching { IngredientCategory.valueOf(value) }.getOrDefault(IngredientCategory.OTHER)

    @TypeConverter
    fun recipeCategoryToString(category: RecipeCategory?): String? = category?.name

    /** Une catégorie disparue d'une version future laisse la recette sans catégorie, sans planter. */
    @TypeConverter
    fun stringToRecipeCategory(value: String?): RecipeCategory? =
        value?.let { runCatching { RecipeCategory.valueOf(it) }.getOrNull() }

    /**
     * Noms joints par une virgule (`SUMMER,AUTUMN`), chaîne vide pour aucune saison. La colonne
     * est `NOT NULL` : Room le déduit du type `Set<Season>`, pas du convertisseur.
     */
    @TypeConverter
    fun seasonsToString(seasons: Set<Season>): String =
        seasons.sortedBy { it.ordinal }.joinToString(",") { it.name }

    /** Une saison disparue d'une version future est simplement ignorée, sans planter. */
    @TypeConverter
    fun stringToSeasons(value: String): Set<Season> =
        value.split(',').mapNotNull { name -> runCatching { Season.valueOf(name) }.getOrNull() }.toSet()
}
