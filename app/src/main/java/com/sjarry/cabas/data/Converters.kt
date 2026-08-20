package com.sjarry.cabas.data

import androidx.room.TypeConverter
import com.sjarry.cabas.parser.IngredientUnit

class Converters {
    @TypeConverter
    fun unitToString(unit: IngredientUnit): String = unit.name

    @TypeConverter
    fun stringToUnit(value: String): IngredientUnit =
        runCatching { IngredientUnit.valueOf(value) }.getOrDefault(IngredientUnit.PIECE)
}
