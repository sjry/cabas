package com.sjarry.cabas.ui.recipes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.ImportReport
import com.sjarry.cabas.data.RecipeRepository
import com.sjarry.cabas.data.SettingsStore
import com.sjarry.cabas.data.entities.RecipeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipesUiState(
    val recipes: List<RecipeEntity> = emptyList(),
    val query: String = "",
    val folderUri: String? = null,
    val isBusy: Boolean = false,
) {
    val visibleRecipes: List<RecipeEntity>
        get() = if (query.isBlank()) recipes
        else recipes.filter { it.title.contains(query.trim(), ignoreCase = true) }
}

class RecipesViewModel(
    private val repository: RecipeRepository,
    private val settings: SettingsStore,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val busy = MutableStateFlow(false)

    /** Dernier bilan d'import, consommé par l'écran pour afficher un message. */
    private val _lastReport = MutableStateFlow<ImportReport?>(null)
    val lastReport: StateFlow<ImportReport?> = _lastReport.asStateFlow()

    val uiState: StateFlow<RecipesUiState> = combine(
        repository.observeRecipes(),
        query,
        settings.folderUri,
        busy,
    ) { recipes, q, folder, isBusy ->
        RecipesUiState(recipes = recipes, query = q, folderUri = folder, isBusy = isBusy)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipesUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

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
