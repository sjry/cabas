package com.sjarry.cabas.ui.shopping

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
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sjarry.cabas.data.model.ShoppingItem
import com.sjarry.cabas.parser.IngredientCategory
import com.sjarry.cabas.ui.AppViewModelProvider
import com.sjarry.cabas.ui.common.EmptyState
import com.sjarry.cabas.ui.common.clickableListItem
import com.sjarry.cabas.ui.common.quantityLabel
import com.sjarry.cabas.ui.common.servingsLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    onGoToMenu: () -> Unit,
    viewModel: ShoppingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val list by viewModel.shoppingList.collectAsStateWithLifecycle()
    val view by viewModel.view.collectAsStateWithLifecycle()
    val takenExpanded by viewModel.takenExpanded.collectAsStateWithLifecycle()

    // L'article dont on est en train de corriger le rayon. État d'affichage local,
    // comme le choix de vue : il n'a aucun sens après un retour à l'écran.
    var editing by remember { mutableStateOf<ShoppingItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liste de courses") },
                actions = {
                    if (list.checkedCount < list.itemCount) {
                        IconButton(onClick = viewModel::checkAll) {
                            Icon(Icons.Filled.DoneAll, contentDescription = "Tout cocher")
                        }
                    }
                    if (list.checkedCount > 0) {
                        IconButton(onClick = viewModel::uncheckAll) {
                            Icon(Icons.Filled.RemoveDone, contentDescription = "Tout décocher")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (list.isEmpty) {
            EmptyState(
                icon = Icons.Filled.Checklist,
                title = "Rien à acheter",
                message = "Composez d'abord un menu : la liste se calcule automatiquement à partir des recettes choisies.",
                modifier = Modifier.padding(padding),
                action = { Button(onClick = onGoToMenu) { Text("Composer le menu") } },
            )
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                // Libellés courts : à trois segments, « Total (A→Z) » ne tient plus
                // sur un écran étroit et se fait tronquer.
                SegmentedButton(
                    selected = view == ShoppingView.AISLE,
                    onClick = { viewModel.showView(ShoppingView.AISLE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                ) { Text("Rayon") }
                SegmentedButton(
                    selected = view == ShoppingView.TOTAL,
                    onClick = { viewModel.showView(ShoppingView.TOTAL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                ) { Text("A→Z") }
                SegmentedButton(
                    selected = view == ShoppingView.BY_RECIPE,
                    onClick = { viewModel.showView(ShoppingView.BY_RECIPE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                ) { Text("Recette") }
            }

            ProgressHeader(checked = list.checkedCount, total = list.itemCount)

            // Les articles pris quittent la liste principale pour la section repliable du
            // bas : en magasin, seul ce qu'il reste à prendre mérite la place à l'écran.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                // L'écran de fin remplace la liste, sauf en vue par recette : là, les
                // articles pris restent affichés sous leur recette, y compris quand
                // tout est coché.
                if (list.isComplete && view != ShoppingView.BY_RECIPE) {
                    item(key = "complete") {
                        CompletedList(count = list.itemCount, onUncheckAll = viewModel::uncheckAll)
                    }
                } else {
                    when (view) {
                        ShoppingView.AISLE -> list.aisles.forEach { aisle ->
                            // Un rayon vidé disparaît purement et simplement : ce n'est
                            // qu'un repère de parcours, pas un jalon du menu comme une
                            // recette terminée.
                            if (aisle.remaining.isEmpty()) return@forEach
                            item(key = "aisle-${aisle.category.name}") {
                                AisleHeader(aisle.category.label)
                            }
                            items(
                                items = aisle.remaining,
                                key = { "aisle-${aisle.category.name}-${it.key}" },
                            ) { item ->
                                ShoppingRow(
                                    item = item,
                                    onLongClick = { editing = item },
                                    onCheckedChange = { checked -> viewModel.toggle(item.key, checked) },
                                )
                            }
                        }

                        ShoppingView.TOTAL -> items(list.remaining, key = { it.key }) { item ->
                            ShoppingRow(
                                item = item,
                                onLongClick = { editing = item },
                                onCheckedChange = { checked -> viewModel.toggle(item.key, checked) },
                            )
                        }

                        // Ici la question n'est plus « que reste-t-il à prendre » mais
                        // « qu'est-ce que cette recette demande » : les articles pris
                        // restent barrés à leur place, sous leur recette.
                        ShoppingView.BY_RECIPE -> list.sections.forEach { section ->
                            item(key = "header-${section.recipeId}") {
                                SectionHeader(section.title, section.servings)
                            }
                            items(
                                items = section.items,
                                key = { "${section.recipeId}-${it.key}" },
                            ) { item ->
                                ShoppingRow(
                                    item = item,
                                    onLongClick = { editing = item },
                                    onCheckedChange = { checked -> viewModel.toggle(item.key, checked) },
                                )
                            }
                        }
                    }
                }

                // La vue par recette garde ses articles pris sur place : les reprendre
                // en bas de liste les afficherait deux fois.
                if (view != ShoppingView.BY_RECIPE && list.taken.isNotEmpty()) {
                    item(key = "taken-header") {
                        TakenHeader(
                            count = list.taken.size,
                            expanded = takenExpanded,
                            onClick = viewModel::toggleTaken,
                        )
                    }
                    if (takenExpanded) {
                        items(list.taken, key = { "taken-${it.key}" }) { item ->
                            ShoppingRow(
                                item = item,
                                onLongClick = { editing = item },
                                onCheckedChange = { checked -> viewModel.toggle(item.key, checked) },
                            )
                        }
                    }
                }
            }
        }

        editing?.let { item ->
            CategorySheet(
                item = item,
                onDismiss = { editing = null },
                onPick = { category ->
                    viewModel.setCategory(item.name, category)
                    editing = null
                },
                onReset = {
                    viewModel.resetCategory(item.name)
                    editing = null
                },
            )
        }
    }
}

/**
 * Correction du rayon d'un article. Ouverte par un appui long : l'action est utile
 * mais rare, elle ne mérite pas de place permanente sur une ligne de liste.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    item: ShoppingItem,
    onDismiss: () -> Unit,
    onPick: (IngredientCategory) -> Unit,
    onReset: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Text(
                text = "Le rayon choisi vaut pour toutes les recettes, et sera retenu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
            )
            IngredientCategory.entries.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableListItem { onPick(category) }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RadioButton(
                        selected = category == item.category,
                        onClick = { onPick(category) },
                    )
                    Text(category.label)
                }
            }
            TextButton(
                onClick = onReset,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            ) { Text("Rayon automatique") }
        }
    }
}

@Composable
private fun ProgressHeader(checked: Int, total: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = "$checked / $total article(s) pris",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else checked.toFloat() / total },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

/** Ce qui s'affiche à la place de la liste quand plus rien ne reste à prendre. */
@Composable
private fun CompletedList(count: Int, onUncheckAll: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingBasket,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Tout est dans le cabas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Les $count articles de la liste sont pris.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        TextButton(onClick = onUncheckAll, modifier = Modifier.padding(top = 8.dp)) {
            Text("Tout décocher")
        }
    }
}

/** En-tête cliquable de la section des articles déjà pris. */
@Composable
private fun TakenHeader(count: Int, expanded: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickableListItem(onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Pris ($count)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Replier les articles pris" else "Afficher les articles pris",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AisleHeader(label: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SectionHeader(title: String, servings: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            servingsLabel(servings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

/**
 * Une ligne de la liste. L'appui long corrige le rayon ; l'appui court coche, parce
 * que `combinedClickable` réclame une action courte et que viser la case à cocher
 * avec un cabas dans l'autre main n'est pas confortable.
 */
@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    onLongClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableListItem(
                onClick = { onCheckedChange(!item.checked) },
                onLongClick = onLongClick,
            )
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Checkbox(checked = item.checked, onCheckedChange = onCheckedChange)
        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            textDecoration = if (item.checked) TextDecoration.LineThrough else null,
            color = if (item.checked) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = quantityLabel(item.quantity, item.unit, item.freeUnitLabel, item.unspecified),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textDecoration = if (item.checked) TextDecoration.LineThrough else null,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
