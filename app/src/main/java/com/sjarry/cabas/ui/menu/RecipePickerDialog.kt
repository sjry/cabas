package com.sjarry.cabas.ui.menu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sjarry.cabas.data.entities.RecipeEntity
import com.sjarry.cabas.ui.common.clickableListItem

/** Sélection multiple des recettes à ajouter au menu. */
@Composable
fun RecipePickerDialog(
    available: List<RecipeEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit,
) {
    var selected by remember { mutableStateOf(emptySet<Long>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter au menu") },
        text = {
            if (available.isEmpty()) {
                Text("Toutes les recettes importées sont déjà dans le menu.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(available, key = { it.id }) { recipe ->
                        val isSelected = recipe.id in selected
                        ListItem(
                            headlineContent = { Text(recipe.title) },
                            leadingContent = {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableListItem {
                                    selected = if (isSelected) selected - recipe.id else selected + recipe.id
                                },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selected) },
                enabled = selected.isNotEmpty(),
            ) { Text("Ajouter (${selected.size})") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}
