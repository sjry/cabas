package com.sjarry.cabas.data

import androidx.room.TypeConverter
import com.sjarry.cabas.parser.IngredientCategory
import com.sjarry.cabas.parser.IngredientUnit

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
}
