package com.sjarry.cabas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sjarry.cabas.data.dao.CheckedItemDao
import com.sjarry.cabas.data.dao.IngredientCategoryDao
import com.sjarry.cabas.data.dao.MenuDao
import com.sjarry.cabas.data.dao.RecipeDao
import com.sjarry.cabas.data.entities.CheckedItemEntity
import com.sjarry.cabas.data.entities.IngredientCategoryEntity
import com.sjarry.cabas.data.entities.IngredientEntity
import com.sjarry.cabas.data.entities.MenuEntryEntity
import com.sjarry.cabas.data.entities.RecipeEntity
import com.sjarry.cabas.data.entities.StepEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        MenuEntryEntity::class,
        CheckedItemEntity::class,
        IngredientCategoryEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun menuDao(): MenuDao
    abstract fun checkedItemDao(): CheckedItemDao
    abstract fun ingredientCategoryDao(): IngredientCategoryDao

    companion object {
        /** Ajout de la case « déjà cuisinée » sur les entrées du menu. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE menu_entries ADD COLUMN done INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Corrections de rayon apprises : le lexique fournit le reste. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS ingredient_categories (" +
                        "name TEXT NOT NULL, category TEXT NOT NULL, PRIMARY KEY(name))",
                )
            }
        }

        /**
         * Type de plat et saisons, lus dans le fichier. Null pour les recettes déjà importées,
         * jusqu'à leur prochaine re-synchro : leur markdown d'alors ne portait pas ces lignes.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN category TEXT")
                db.execSQL("ALTER TABLE recipes ADD COLUMN seasons TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "cabas.db",
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
