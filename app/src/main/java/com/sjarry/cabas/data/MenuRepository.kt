package com.sjarry.cabas.data

import com.sjarry.cabas.data.dao.MenuEntryWithRecipe
import com.sjarry.cabas.data.entities.CheckedItemEntity
import com.sjarry.cabas.data.entities.MenuEntryEntity
import com.sjarry.cabas.data.model.MenuRecipe
import com.sjarry.cabas.data.model.ShoppingList
import com.sjarry.cabas.parser.ParsedIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Le menu courant : des recettes et, pour chacune, un nombre de convives.
 * L'application n'en gère qu'un seul, il n'y a donc pas de table « menu ».
 */
class MenuRepository(private val db: AppDatabase) {

    private val menuDao = db.menuDao()
    private val checkedDao = db.checkedItemDao()

    fun observeMenu(): Flow<List<MenuRecipe>> =
        menuDao.observeMenu().map { entries -> entries.map { it.toMenuRecipe() } }

    fun observeMenuRecipeIds(): Flow<Set<Long>> =
        menuDao.observeMenuRecipeIds().map { it.toSet() }

    /** Liste de courses recalculée à chaque changement du menu ou des cases cochées. */
    fun observeShoppingList(): Flow<ShoppingList> =
        combine(observeMenu(), checkedDao.observeChecked()) { menu, checked ->
            ShoppingListBuilder.build(menu, checked.toSet())
        }

    suspend fun addRecipes(recipeIds: Collection<Long>, servings: Int = DEFAULT_SERVINGS) =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            recipeIds.forEachIndexed { index, id ->
                menuDao.addEntry(MenuEntryEntity(recipeId = id, servings = servings, addedAt = now + index))
            }
        }

    suspend fun setServings(recipeId: Long, servings: Int) = withContext(Dispatchers.IO) {
        menuDao.updateServings(recipeId, servings.coerceIn(MIN_SERVINGS, MAX_SERVINGS))
        pruneCheckedItems()
    }

    /** Marque une recette du menu comme déjà cuisinée, ou l'inverse. */
    suspend fun setDone(recipeId: Long, done: Boolean) = withContext(Dispatchers.IO) {
        menuDao.updateDone(recipeId, done)
    }

    suspend fun removeRecipe(recipeId: Long) = withContext(Dispatchers.IO) {
        menuDao.removeEntry(recipeId)
        pruneCheckedItems()
    }

    suspend fun clearMenu() = withContext(Dispatchers.IO) {
        menuDao.clearMenu()
        checkedDao.clear()
    }

    suspend fun setChecked(key: String, checked: Boolean) = withContext(Dispatchers.IO) {
        if (checked) checkedDao.check(CheckedItemEntity(key)) else checkedDao.uncheck(key)
    }

    /** Coche tous les articles de la liste courante. */
    suspend fun checkAll(keys: Collection<String>) = withContext(Dispatchers.IO) {
        checkedDao.checkAll(keys.map { CheckedItemEntity(it) })
    }

    suspend fun uncheckAll() = withContext(Dispatchers.IO) { checkedDao.clear() }

    /** Retire les cases cochées dont l'article n'est plus dans la liste. */
    private suspend fun pruneCheckedItems() {
        val menu = menuDao.observeMenu().first().map { it.toMenuRecipe() }
        val keys = ShoppingListBuilder.aggregate(menu).map { it.key }
        if (keys.isEmpty()) checkedDao.clear() else checkedDao.keepOnly(keys)
    }

    companion object {
        const val DEFAULT_SERVINGS = 2
        const val MIN_SERVINGS = 1
        const val MAX_SERVINGS = 50
    }
}

private fun MenuEntryWithRecipe.toMenuRecipe(): MenuRecipe = MenuRecipe(
    recipeId = entry.recipeId,
    title = recipe.recipe.title,
    servings = entry.servings,
    done = entry.done,
    ingredients = recipe.ingredients
        .sortedBy { it.position }
        .map {
            ParsedIngredient(
                name = it.name,
                quantity = it.quantity,
                unit = it.unit,
                freeUnitLabel = it.freeUnitLabel,
                unspecified = it.unspecified,
                rawLine = it.rawLine,
            )
        },
)
