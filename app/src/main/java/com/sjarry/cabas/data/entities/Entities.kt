package com.sjarry.cabas.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sjarry.cabas.parser.IngredientUnit

/** Une recette importée. Les quantités qu'elle porte sont toujours pour 1 personne. */
@Entity(tableName = "recipes", indices = [Index(value = ["sourceUri"], unique = true)])
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** URI du fichier d'origine, null pour une recette collée à la main. */
    val sourceUri: String? = null,
    val sourceFileName: String? = null,
    val rawMarkdown: String,
    val importedAt: Long,
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId")],
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String,
    /** Quantité pour 1 personne, en unité canonique (g ou ml). */
    val quantity: Double,
    val unit: IngredientUnit,
    val freeUnitLabel: String? = null,
    val unspecified: Boolean = false,
    val rawLine: String = "",
    val position: Int,
)

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId")],
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val text: String,
    val position: Int,
)

/** Une recette du menu courant. L'application n'en gère qu'un seul à la fois. */
@Entity(
    tableName = "menu_entries",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MenuEntryEntity(
    @PrimaryKey val recipeId: Long,
    val servings: Int,
    val addedAt: Long,
    /** Recette déjà cuisinée : cochée dans le menu. */
    val done: Boolean = false,
)

/** Article coché dans la liste de courses. La clé survit au recalcul de la liste. */
@Entity(tableName = "checked_items")
data class CheckedItemEntity(
    @PrimaryKey val itemKey: String,
)
