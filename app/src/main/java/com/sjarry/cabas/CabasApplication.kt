package com.sjarry.cabas

import android.app.Application
import com.sjarry.cabas.data.AppDatabase
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.SettingsStore

/** Conteneur de dépendances minimal : l'application est trop petite pour Hilt. */
class CabasApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database = AppDatabase.get(application)
    val recipeRepository = RecipeRepository(application, database)
    val menuRepository = MenuRepository(database)
    val settingsStore = SettingsStore(application)
}
