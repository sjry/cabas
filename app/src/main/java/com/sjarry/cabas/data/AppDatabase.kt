package com.sjarry.cabas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sjarry.cabas.data.dao.CheckedItemDao
import com.sjarry.cabas.data.dao.MenuDao
import com.sjarry.cabas.data.dao.RecipeDao
import com.sjarry.cabas.data.entities.CheckedItemEntity
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
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun menuDao(): MenuDao
    abstract fun checkedItemDao(): CheckedItemDao

    companion object {
        /** Ajout de la case « déjà cuisinée » sur les entrées du menu. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE menu_entries ADD COLUMN done INTEGER NOT NULL DEFAULT 0")
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
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
