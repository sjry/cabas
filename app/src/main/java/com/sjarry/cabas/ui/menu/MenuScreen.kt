package com.sjarry.cabas.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.ui.AppViewModelProvider
import com.sjarry.cabas.ui.common.EmptyState
import com.sjarry.cabas.ui.common.servingsLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onOpenShoppingList: () -> Unit,
    onGoToRecipes: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    viewModel: MenuViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu") },
                actions = {
                    if (!state.isEmpty) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Vider le menu")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                !state.hasRecipes -> EmptyState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Aucune recette importée",
                    message = "Importez d'abord des recettes pour pouvoir composer un menu.",
                    action = {
                        Button(onClick = onGoToRecipes) { Text("Aller aux recettes") }
                    },
                )

                state.isEmpty -> EmptyState(
                    icon = Icons.Filled.RestaurantMenu,
                    title = "Menu vide",
                    message = "Ajoutez des recettes et indiquez pour combien de personnes vous cuisinez.",
                    action = {
                        Button(onClick = { showPicker = true }) { Text("Ajouter des recettes") }
                    },
                )

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.menu, key = { it.recipeId }) { entry ->
                            MenuRecipeCard(
                                title = entry.title,
                                servings = entry.servings,
                                ingredientCount = entry.ingredients.size,
                                onServingsChange = { viewModel.changeServings(entry.recipeId, it) },
                                onRemove = { viewModel.removeRecipe(entry.recipeId) },
                                onOpen = { onOpenRecipe(entry.recipeId) },
                                done = entry.done,
                                onDoneChange = { viewModel.setDone(entry.recipeId, it) },
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { showPicker = true },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Text("Ajouter", modifier = Modifier.padding(start = 8.dp))
                        }
                        Button(
                            onClick = onOpenShoppingList,
                            modifier = Modifier.weight(1.4f),
                        ) {
                            Text("Liste de courses")
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        RecipePickerDialog(
            available = state.available,
            onDismiss = { showPicker = false },
            onConfirm = { ids ->
                showPicker = false
                if (ids.isNotEmpty()) viewModel.addRecipes(ids)
            },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Vider le menu ?") },
            text = { Text("Les recettes sélectionnées et les articles cochés seront retirés. Les recettes importées ne sont pas supprimées.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearMenu()
                    confirmClear = false
                }) { Text("Vider") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Annuler") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuRecipeCard(
    title: String,
    servings: Int,
    ingredientCount: Int,
    onServingsChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onOpen: () -> Unit,
    done: Boolean,
    onDoneChange: (Boolean) -> Unit,
) {
    // Une recette déjà cuisinée est estompée, sans disparaître ni changer de place.
    val contentAlpha = if (done) 0.45f else 1f

    // La carte entière ouvre la recette ; les contrôles internes (case, retrait,
    // convives) consomment leur propre clic et ne déclenchent donc pas l'ouverture.
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(start = 4.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = done,
                    onCheckedChange = onDoneChange,
                    modifier = Modifier.semantics {
                        contentDescription = if (done) "Recette faite" else "Marquer comme faite"
                    },
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 12.dp)
                        .alpha(contentAlpha),
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$ingredientCount ingrédient(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Retirer du menu")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 12.dp)
                    .alpha(contentAlpha),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(
                    onClick = { onServingsChange(servings - 1) },
                    enabled = servings > MenuRepository.MIN_SERVINGS,
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Une personne de moins")
                }
                Text(
                    text = servingsLabel(servings),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(120.dp),
                )
                FilledTonalIconButton(
                    onClick = { onServingsChange(servings + 1) },
                    enabled = servings < MenuRepository.MAX_SERVINGS,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Une personne de plus")
                }
            }
        }
    }
}
