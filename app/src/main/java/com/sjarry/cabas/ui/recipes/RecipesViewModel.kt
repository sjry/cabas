package com.sjarry.cabas.ui.recipes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.ImportReport
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.SettingsStore
import com.sjarry.cabas.data.entities.RecipeEntity
import com.sjarry.cabas.parser.RecipeCategory
import com.sjarry.cabas.parser.Season
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipesUiState(
    val recipes: List<RecipeEntity> = emptyList(),
    val query: String = "",
    /** Catégorie filtrée, toujours parmi [categories] ; null sans filtre. */
    val category: RecipeCategory? = null,
    /** Saison filtrée, toujours parmi [seasons] ; null sans filtre. */
    val season: Season? = null,
    val folderUri: String? = null,
    val isBusy: Boolean = false,
) {
    /** Les catégories présentes dans la bibliothèque : les seules puces qui rendraient quelque chose. */
    val categories: List<RecipeCategory>
        get() = RecipeCategory.present(recipes.map { it.category })

    /** Même principe pour les saisons. */
    val seasons: List<Season>
        get() = Season.present(recipes.map { it.seasons })

    val isFiltering: Boolean
        get() = query.isNotBlank() || category != null || season != null

    val visibleRecipes: List<RecipeEntity>
        get() = recipes.filter {
            (category == null || it.category == category) &&
                (season == null || season in it.seasons) &&
                (query.isBlank() || it.title.contains(query.trim(), ignoreCase = true))
        }
}

/**
 * Les trois filtres de l'écran dans un seul flux : `combine` n'a pas de surcharge typée
 * au-delà de cinq flux, et recettes, dossier et import en occupent déjà trois.
 */
private data class Filters(
    val query: String = "",
    val category: RecipeCategory? = null,
    val season: Season? = null,
)

class RecipesViewModel(
    private val repository: RecipeRepository,
    private val settings: SettingsStore,
) : ViewModel() {

    private val filters = MutableStateFlow(Filters())
    private val busy = MutableStateFlow(false)

    /** Dernier bilan d'import, consommé par l'écran pour afficher un message. */
    private val _lastReport = MutableStateFlow<ImportReport?>(null)
    val lastReport: StateFlow<ImportReport?> = _lastReport.asStateFlow()

    val uiState: StateFlow<RecipesUiState> = combine(
        repository.observeRecipes(),
        filters,
        settings.folderUri,
        busy,
    ) { recipes, f, folder, isBusy ->
        RecipesUiState(
            recipes = recipes,
            query = f.query,
            // Un filtre dont la dernière recette vient d'être supprimée n'a plus de puce :
            // il s'éteint avec elle plutôt que de laisser une liste vide inexplicable.
            category = f.category?.takeIf { c -> recipes.any { it.category == c } },
            season = f.season?.takeIf { s -> recipes.any { s in it.seasons } },
            folderUri = folder,
            isBusy = isBusy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipesUiState(),
    )

    fun onQueryChange(value: String) = filters.update { it.copy(query = value) }

    fun onCategoryChange(value: RecipeCategory?) = filters.update { it.copy(category = value) }

    fun onSeasonChange(value: Season?) = filters.update { it.copy(season = value) }

    fun importFiles(uris: List<Uri>) = runImport { repository.importFromUris(uris) }

    fun importText(markdown: String) = runImport { repository.importFromText(markdown) }

    /** Mémorise le dossier choisi puis le synchronise immédiatement. */
    fun selectFolder(treeUri: Uri) = viewModelScope.launch {
        settings.setFolderUri(treeUri.toString())
        runImport { repository.syncFolder(treeUri) }
    }

    fun syncFolder() {
        val uri = uiState.value.folderUri ?: return
        runImport { repository.syncFolder(Uri.parse(uri)) }
    }

    fun forgetFolder() = viewModelScope.launch { settings.setFolderUri(null) }

    fun deleteRecipe(id: Long) = viewModelScope.launch { repository.deleteRecipe(id) }

    fun consumeReport() {
        _lastReport.value = null
    }

    private fun runImport(block: suspend () -> ImportReport) = viewModelScope.launch {
        busy.value = true
        try {
            _lastReport.value = block()
        } finally {
            busy.value = false
        }
    }
}
