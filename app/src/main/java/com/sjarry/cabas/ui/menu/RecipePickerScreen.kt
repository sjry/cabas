package com.sjarry.cabas.ui.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sjarry.cabas.data.RecipeSearch
import com.sjarry.cabas.ui.AppViewModelProvider
import com.sjarry.cabas.ui.common.EmptyState
import com.sjarry.cabas.ui.common.clickableListItem

/**
 * Sélection des recettes à ajouter au menu. La liste peut compter des centaines de recettes : il
 * faut de la place, une recherche, des repères pour se situer et un rappel permanent de ce qui est
 * déjà coché.
 *
 * C'est un écran à part entière, pas un dialogue : un dialogue plein écran demande de recoller à
 * la main les encarts système et le clavier, et sa barre basse finit sous le bord de l'écran. Ici
 * la navigation s'en charge comme pour les autres écrans, et le geste de retour fonctionne.
 *
 * Son [RecipePickerViewModel] est propre à la destination : la recherche naît et meurt avec
 * l'écran, il n'y a rien à remettre à zéro en sortant.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipePickerScreen(
    onDone: () -> Unit,
    viewModel: RecipePickerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Les titres accompagnent les identifiants : une recette cochée puis écartée par la recherche
    // doit rester nommée dans les puces du bas, on ne peut donc pas les relire dans `sections`.
    var selected by remember { mutableStateOf(emptyMap<Long, String>()) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Ajouter au menu") },
                    navigationIcon = {
                        IconButton(onClick = onDone) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    },
                )
                if (state.availableCount > 0) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        // Le libellé, et non un placeholder : Material masque le placeholder tant
                        // que le champ n'a pas le focus, et c'est justement à ce moment-là qu'il
                        // faut annoncer qu'on peut chercher un ingrédient.
                        label = { Text("Titre ou ingrédient") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Effacer la recherche")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        },
    ) { padding ->
        // En bas, l'encart de `safeDrawing` plutôt que celui du Scaffold : il vaut déjà le plus
        // grand du clavier et de la barre de navigation. Cumuler `padding` et `imePadding`
        // laisserait, clavier ouvert, la hauteur de la barre de navigation en vide inutile.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        ) {
            // La liste cède la place à la barre de sélection, quoi qu'elle contienne.
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.availableCount == 0 -> EmptyState(
                        icon = Icons.Filled.RestaurantMenu,
                        title = "Rien à ajouter",
                        message = "Toutes les recettes importées sont déjà dans le menu.",
                    )

                    state.sections.isEmpty() -> EmptyState(
                        icon = Icons.Filled.Search,
                        title = "Aucun résultat",
                        message = "Aucune recette ne correspond à « ${state.query} », " +
                            "ni par son titre ni par ses ingrédients.",
                    )

                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        state.sections.forEach { section ->
                            // En-tête d'initiale seulement hors recherche : les résultats sont
                            // classés par pertinence, pas par alphabet.
                            section.header?.let { header ->
                                stickyHeader(key = "header-$header") { SectionHeader(header) }
                            }
                            items(section.matches, key = { it.id }) { match ->
                                val isSelected = match.id in selected
                                MatchRow(
                                    match = match,
                                    selected = isSelected,
                                    onToggle = {
                                        selected = if (isSelected) {
                                            selected - match.id
                                        } else {
                                            selected + (match.id to match.title)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            SelectionBar(
                selected = selected,
                onUnselect = { id -> selected = selected - id },
                onConfirm = {
                    viewModel.addRecipes(selected.keys)
                    onDone()
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(header: String) {
    // Opaque : les lignes doivent passer dessous sans transparaître pendant le défilement.
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = header,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun MatchRow(
    match: RecipeSearch.Match,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(match.title) },
        supportingContent = {
            Text(
                // Quand c'est un ingrédient qui a fait remonter la recette, le dire : sans ça, un
                // résultat dont le titre ne contient pas la saisie paraît arbitraire.
                text = match.matchedIngredient?.let { "Ingrédient : $it" }
                    ?: "${match.ingredientCount} ingrédient(s)",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = { Checkbox(checked = selected, onCheckedChange = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().clickableListItem(onToggle),
    )
}

/** Barre basse : ce qui est coché reste visible et retirable, où qu'on soit dans la liste. */
@Composable
private fun SelectionBar(
    selected: Map<Long, String>,
    onUnselect: (Long) -> Unit,
    onConfirm: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            if (selected.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selected.forEach { (id, title) ->
                        InputChip(
                            selected = true,
                            onClick = { onUnselect(id) },
                            label = { Text(title) },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Retirer $title de la sélection",
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                }
            }

            Button(
                onClick = onConfirm,
                enabled = selected.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            ) { Text("Ajouter au menu (${selected.size})") }
        }
    }
}
