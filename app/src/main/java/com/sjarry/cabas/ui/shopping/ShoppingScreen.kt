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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sjarry.cabas.data.model.ShoppingItem
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
                SegmentedButton(
                    selected = view == ShoppingView.TOTAL,
                    onClick = { viewModel.showView(ShoppingView.TOTAL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Total (A→Z)") }
                SegmentedButton(
                    selected = view == ShoppingView.BY_RECIPE,
                    onClick = { viewModel.showView(ShoppingView.BY_RECIPE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Par recette") }
            }

            ProgressHeader(checked = list.checkedCount, total = list.itemCount)

            // Les articles pris quittent la liste principale pour la section repliable du
            // bas : en magasin, seul ce qu'il reste à prendre mérite la place à l'écran.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (list.isComplete) {
                    item(key = "complete") {
                        CompletedList(count = list.itemCount, onUncheckAll = viewModel::uncheckAll)
                    }
                } else {
                    when (view) {
                        ShoppingView.TOTAL -> items(list.remaining, key = { it.key }) { item ->
                            ShoppingRow(item) { checked -> viewModel.toggle(item.key, checked) }
                        }

                        ShoppingView.BY_RECIPE -> list.sections.forEach { section ->
                            // Une recette dont tout est pris se réduit à une ligne : elle
                            // reste visible à sa place, sans occuper dix lignes barrées.
                            if (section.remaining.isEmpty()) {
                                item(key = "done-${section.recipeId}") {
                                    CompletedSectionRow(section.title)
                                }
                            } else {
                                item(key = "header-${section.recipeId}") {
                                    SectionHeader(section.title, section.servings)
                                }
                                items(
                                    items = section.remaining,
                                    key = { "${section.recipeId}-${it.key}" },
                                ) { item ->
                                    ShoppingRow(item) { checked -> viewModel.toggle(item.key, checked) }
                                }
                            }
                        }
                    }
                }

                if (list.taken.isNotEmpty()) {
                    item(key = "taken-header") {
                        TakenHeader(
                            count = list.taken.size,
                            expanded = takenExpanded,
                            onClick = viewModel::toggleTaken,
                        )
                    }
                    if (takenExpanded) {
                        items(list.taken, key = { "taken-${it.key}" }) { item ->
                            ShoppingRow(item) { checked -> viewModel.toggle(item.key, checked) }
                        }
                    }
                }
            }
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

/** Vue « Par recette » : la recette entièrement achetée, réduite à une ligne. */
@Composable
private fun CompletedSectionRow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "complet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
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

@Composable
private fun ShoppingRow(item: ShoppingItem, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
