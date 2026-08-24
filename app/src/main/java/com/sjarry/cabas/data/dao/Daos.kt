package com.sjarry.cabas.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.sjarry.cabas.data.entities.CheckedItemEntity
import com.sjarry.cabas.data.entities.IngredientCategoryEntity
import com.sjarry.cabas.data.entities.IngredientEntity
import com.sjarry.cabas.data.entities.MenuEntryEntity
import com.sjarry.cabas.data.entities.RecipeEntity
import com.sjarry.cabas.data.entities.StepEntity
import kotlinx.coroutines.flow.Flow

/** Une recette avec ses ingrédients et ses étapes. */
data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val ingredients: List<IngredientEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val steps: List<StepEntity>,
)

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeWithDetails(id: Long): Flow<RecipeWithDetails?>

    @Query("SELECT * FROM recipes WHERE sourceUri = :uri LIMIT 1")
    suspend fun findBySourceUri(uri: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE title = :title AND sourceUri IS NULL LIMIT 1")
    suspend fun findPastedByTitle(title: String): RecipeEntity?

    @Query("SELECT sourceUri FROM recipes WHERE sourceUri IS NOT NULL")
    suspend fun allSourceUris(): List<String?>

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Query("UPDATE recipes SET title = :title, rawMarkdown = :markdown, sourceFileName = :fileName, importedAt = :importedAt WHERE id = :id")
    suspend fun updateRecipe(id: Long, title: String, markdown: String, fileName: String?, importedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(items: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(items: List<StepEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsOf(recipeId: Long)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsOf(recipeId: Long)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    @Query("DELETE FROM recipes WHERE sourceUri IN (:uris)")
    suspend fun deleteBySourceUris(uris: List<String>)
}

/** Une entrée du menu jointe à sa recette complète. */
data class MenuEntryWithRecipe(
    @Embedded val entry: MenuEntryEntity,
    @Relation(parentColumn = "recipeId", entityColumn = "id", entity = RecipeEntity::class)
    val recipe: RecipeWithDetails,
)

@Dao
interface MenuDao {

    @Transaction
    @Query("SELECT * FROM menu_entries ORDER BY addedAt ASC")
    fun observeMenu(): Flow<List<MenuEntryWithRecipe>>

    @Query("SELECT recipeId FROM menu_entries")
    fun observeMenuRecipeIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEntry(entry: MenuEntryEntity)

    @Query("UPDATE menu_entries SET servings = :servings WHERE recipeId = :recipeId")
    suspend fun updateServings(recipeId: Long, servings: Int)

    @Query("UPDATE menu_entries SET done = :done WHERE recipeId = :recipeId")
    suspend fun updateDone(recipeId: Long, done: Boolean)

    @Query("DELETE FROM menu_entries WHERE recipeId = :recipeId")
    suspend fun removeEntry(recipeId: Long)

    @Query("DELETE FROM menu_entries")
    suspend fun clearMenu()
}

@Dao
interface CheckedItemDao {

    @Query("SELECT itemKey FROM checked_items")
    fun observeChecked(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun check(item: CheckedItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun checkAll(items: List<CheckedItemEntity>)

    @Query("DELETE FROM checked_items WHERE itemKey = :key")
    suspend fun uncheck(key: String)

    @Query("DELETE FROM checked_items")
    suspend fun clear()

    @Query("DELETE FROM checked_items WHERE itemKey NOT IN (:keys)")
    suspend fun keepOnly(keys: List<String>)
}

@Dao
interface IngredientCategoryDao {

    @Query("SELECT * FROM ingredient_categories")
    fun observeAll(): Flow<List<IngredientCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: IngredientCategoryEntity)

    /** Oublie la correction : l'ingrédient repasse au rayon deviné par le lexique. */
    @Query("DELETE FROM ingredient_categories WHERE name = :name")
    suspend fun delete(name: String)
}
