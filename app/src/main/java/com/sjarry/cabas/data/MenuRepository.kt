package com.sjarry.cabas.data

import androidx.room.withTransaction
import com.sjarry.cabas.data.dao.MenuEntryWithRecipe
import com.sjarry.cabas.data.entities.CheckedItemEntity
import com.sjarry.cabas.data.entities.IngredientCategoryEntity
import com.sjarry.cabas.data.entities.MenuEntryEntity
import com.sjarry.cabas.data.model.MenuRecipe
import com.sjarry.cabas.data.model.ShoppingList
import com.sjarry.cabas.parser.IngredientCategory
import com.sjarry.cabas.parser.ParsedIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Le menu courant : des recettes et, pour chacune, un nombre de convives.
 * L'application n'en gère qu'un seul, il n'y a donc pas de table « menu ».
 */
class MenuRepository(private val db: AppDatabase) {

    private val menuDao = db.menuDao()
    private val recipeDao = db.recipeDao()
    private val checkedDao = db.checkedItemDao()
    private val categoryDao = db.ingredientCategoryDao()

    fun observeMenu(): Flow<List<MenuRecipe>> =
        menuDao.observeMenu().map { entries -> entries.map { it.toMenuRecipe() } }

    fun observeMenuRecipeIds(): Flow<Set<Long>> =
        menuDao.observeMenuRecipeIds().map { it.toSet() }

    /**
     * Liste de courses recalculée à chaque changement du menu, des cases cochées ou
     * des rayons corrigés.
     */
    fun observeShoppingList(): Flow<ShoppingList> =
        combine(
            observeMenu(),
            checkedDao.observeChecked(),
            categoryDao.observeAll(),
        ) { menu, checked, categories ->
            ShoppingListBuilder.build(
                menu = menu,
                checkedKeys = checked.toSet(),
                categoryOverrides = categories.associate { it.name to it.category },
            )
        }

    suspend fun addRecipes(recipeIds: Collection<Long>, servings: Int = DEFAULT_SERVINGS) =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            recipeIds.forEachIndexed { index, id ->
                menuDao.addEntry(MenuEntryEntity(recipeId = id, servings = servings, addedAt = now + index))
            }
        }

    /**
     * Tire au sort des recettes hors menu et les ajoute, toutes pour le même nombre de convives
     * [servings] : le tirage sert à composer les repas d'une même table. La base est relue au
     * moment du tirage plutôt que de faire confiance à l'état de l'écran : le menu a pu changer
     * entre-temps.
     *
     * @return les identifiants réellement ajoutés — moins que [count] si la bibliothèque
     * n'en avait pas assez hors menu.
     */
    suspend fun addRandomRecipes(
        count: Int,
        servings: Int = DEFAULT_SERVINGS,
        random: Random = Random.Default,
    ): List<Long> =
        withContext(Dispatchers.IO) {
            val drawn = MenuDraw.draw(
                allRecipeIds = recipeDao.allRecipeIds(),
                inMenu = menuDao.menuRecipeIds().toSet(),
                count = count,
                random = random,
            )
            addRecipes(drawn, servings.coerceIn(MIN_SERVINGS, MAX_SERVINGS))
            drawn
        }

    /**
     * Remplace une recette du menu par une autre, tirée au sort hors menu. Le créneau ne bouge
     * pas : mêmes convives, même place dans la liste — c'est ce qui rend le changement lisible
     * sans confirmation, la carte se réécrit sous le doigt au lieu de sauter en bas. La case
     * « faite » repart à zéro, elle parlait de l'autre plat.
     *
     * Les deux écritures tiennent dans une transaction, sinon le menu passerait par un état à
     * une recette de moins et la carte clignoterait.
     *
     * @return l'identifiant de la remplaçante, ou null s'il n'y avait rien à tirer.
     */
    suspend fun swapRecipe(recipeId: Long, random: Random = Random.Default): Long? =
        withContext(Dispatchers.IO) {
            val replacement = db.withTransaction {
                val slot = menuDao.findEntry(recipeId) ?: return@withTransaction null
                val drawn = MenuDraw.draw(
                    allRecipeIds = recipeDao.allRecipeIds(),
                    inMenu = menuDao.menuRecipeIds().toSet(),
                    count = 1,
                    random = random,
                ).firstOrNull() ?: return@withTransaction null

                menuDao.removeEntry(recipeId)
                menuDao.addEntry(
                    MenuEntryEntity(recipeId = drawn, servings = slot.servings, addedAt = slot.addedAt),
                )
                drawn
            }
            // Hors transaction : la purge relit le menu par son `Flow`.
            if (replacement != null) pruneCheckedItems()
            replacement
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

    /**
     * Corrige le rayon d'un ingrédient, pour de bon. Rangé sous le nom normalisé : la
     * correction vaut dans toutes les recettes, et n'est jamais purgée avec le menu —
     * contrairement aux cases cochées, elle décrit le magasin, pas les courses en cours.
     */
    suspend fun setCategory(name: String, category: IngredientCategory) =
        withContext(Dispatchers.IO) {
            categoryDao.upsert(
                IngredientCategoryEntity(ShoppingListBuilder.normalizeName(name), category),
            )
        }

    /** Rend l'ingrédient au lexique. */
    suspend fun resetCategory(name: String) = withContext(Dispatchers.IO) {
        categoryDao.delete(ShoppingListBuilder.normalizeName(name))
    }

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
