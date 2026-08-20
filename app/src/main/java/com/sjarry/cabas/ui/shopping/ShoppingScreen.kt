package com.sjarry.cabas.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RemoveDone
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sjarry.cabas.data.model.ShoppingItem
import com.sjarry.cabas.ui.AppViewModelProvider
import com.sjarry.cabas.ui.common.EmptyState
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                when (view) {
                    ShoppingView.TOTAL -> items(list.total, key = { it.key }) { item ->
                        ShoppingRow(item) { checked -> viewModel.toggle(item.key, checked) }
                    }

                    ShoppingView.BY_RECIPE -> list.sections.forEach { section ->
                        item(key = "header-${section.recipeId}") {
                            SectionHeader(section.title, section.servings)
                        }
                        items(
                            items = section.items,
                            key = { "${section.recipeId}-${it.key}" },
                        ) { item ->
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
