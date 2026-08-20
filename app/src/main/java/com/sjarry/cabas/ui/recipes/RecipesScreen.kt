package com.sjarry.cabas.ui.recipes

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sjarry.cabas.data.entities.RecipeEntity
import com.sjarry.cabas.ui.AppViewModelProvider
import com.sjarry.cabas.ui.common.EmptyState
import com.sjarry.cabas.ui.common.clickableListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onOpenRecipe: (Long) -> Unit,
    viewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val report by viewModel.lastReport.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showImportSheet by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<RecipeEntity?>(null) }

    val pickFiles = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.importFiles(uris)
    }

    val pickFolder = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            // Sans cette permission persistante, le dossier serait inaccessible au prochain lancement.
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.selectFolder(uri)
        }
    }

    LaunchedEffect(report) {
        report?.let {
            snackbarHostState.showSnackbar(it.summary())
            viewModel.consumeReport()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recettes") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showImportSheet = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Importer") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (state.recipes.isNotEmpty()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text("Rechercher") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (state.isBusy) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Text("Import en cours…", style = MaterialTheme.typography.bodyMedium)
                }
            }

            when {
                state.recipes.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Description,
                    title = "Aucune recette",
                    message = "Importez vos fichiers markdown avec le bouton « Importer ». " +
                        "Une recette = un fichier, avec un titre, une section Ingrédients et une section Étapes.",
                )

                state.visibleRecipes.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Search,
                    title = "Aucun résultat",
                    message = "Aucune recette ne correspond à « ${state.query} ».",
                )

                else -> LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    items(state.visibleRecipes, key = { it.id }) { recipe ->
                        ListItem(
                            headlineContent = { Text(recipe.title) },
                            supportingContent = {
                                Text(
                                    text = recipe.sourceFileName ?: "Recette collée",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { recipeToDelete = recipe }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableListItem { onOpenRecipe(recipe.id) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showImportSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showImportSheet = false },
            sheetState = sheetState,
        ) {
            ImportSheetContent(
                folderUri = state.folderUri,
                onPickFiles = {
                    showImportSheet = false
                    // Les .md n'ont pas de type MIME fiable selon les fournisseurs : on ouvre large.
                    pickFiles.launch(arrayOf("*/*"))
                },
                onPickFolder = {
                    showImportSheet = false
                    pickFolder.launch(null)
                },
                onSyncFolder = {
                    showImportSheet = false
                    viewModel.syncFolder()
                },
                onForgetFolder = {
                    showImportSheet = false
                    viewModel.forgetFolder()
                },
                onPasteText = {
                    showImportSheet = false
                    showPasteDialog = true
                },
            )
        }
    }

    if (showPasteDialog) {
        PasteRecipeDialog(
            onDismiss = { showPasteDialog = false },
            onImport = { markdown ->
                showPasteDialog = false
                viewModel.importText(markdown)
            },
        )
    }

    recipeToDelete?.let { recipe ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text("Supprimer la recette ?") },
            text = { Text("« ${recipe.title} » sera retirée de l'application et du menu courant.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe(recipe.id)
                    recipeToDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun ImportSheetContent(
    folderUri: String?,
    onPickFiles: () -> Unit,
    onPickFolder: () -> Unit,
    onSyncFolder: () -> Unit,
    onForgetFolder: () -> Unit,
    onPasteText: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            "Importer des recettes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )

        ListItem(
            headlineContent = { Text("Choisir des fichiers .md") },
            supportingContent = { Text("Sélection multiple possible") },
            leadingContent = { Icon(Icons.Filled.Description, contentDescription = null) },
            modifier = Modifier.clickableListItem(onPickFiles),
        )

        if (folderUri == null) {
            ListItem(
                headlineContent = { Text("Synchroniser un dossier") },
                supportingContent = { Text("Tous les .md du dossier, re-importables à tout moment") },
                leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                modifier = Modifier.clickableListItem(onPickFolder),
            )
        } else {
            ListItem(
                headlineContent = { Text("Re-synchroniser le dossier") },
                supportingContent = { Text(folderUri.readableFolderName()) },
                leadingContent = { Icon(Icons.Filled.Sync, contentDescription = null) },
                modifier = Modifier.clickableListItem(onSyncFolder),
            )
            ListItem(
                headlineContent = { Text("Changer de dossier") },
                leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                modifier = Modifier.clickableListItem(onPickFolder),
            )
            ListItem(
                headlineContent = { Text("Oublier ce dossier") },
                leadingContent = { Icon(Icons.Filled.LinkOff, contentDescription = null) },
                modifier = Modifier.clickableListItem(onForgetFolder),
            )
        }

        ListItem(
            headlineContent = { Text("Coller du texte") },
            supportingContent = { Text("Pour une recette copiée depuis une autre application") },
            leadingContent = { Icon(Icons.Filled.ContentPaste, contentDescription = null) },
            modifier = Modifier.clickableListItem(onPasteText),
        )
    }
}

/** Affiche la fin de l'URI du dossier, seule partie lisible pour un humain. */
private fun String.readableFolderName(): String =
    substringAfterLast("%3A").ifBlank { substringAfterLast('/') }
        .replace("%2F", "/")
