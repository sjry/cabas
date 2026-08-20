package com.sjarry.cabas.ui.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sjarry.cabas.parser.RecipeParser
import com.sjarry.cabas.ui.common.quantityLabel

/**
 * Collage d'une recette markdown, avec un aperçu du résultat du parsing :
 * l'utilisateur voit ce que l'application a compris avant de valider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasteRecipeDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit,
) {
    var markdown by remember { mutableStateOf("") }
    val preview = remember(markdown) {
        if (markdown.isBlank()) null else RecipeParser.parse(markdown, fallbackTitle = "Recette collée")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Coller une recette") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Fermer")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { onImport(markdown) },
                            enabled = preview != null && preview.ingredients.isNotEmpty(),
                        ) { Text("Importer") }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                OutlinedTextField(
                    value = markdown,
                    onValueChange = { markdown = it },
                    label = { Text("Markdown de la recette") },
                    placeholder = { Text("# Titre\n\n## Ingrédients\n- 100 g de farine\n\n## Étapes\n1. …") },
                    minLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (preview != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Aperçu", style = MaterialTheme.typography.labelLarge)
                            Text(
                                preview.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                "${preview.ingredients.size} ingrédient(s), ${preview.steps.size} étape(s) — pour 1 personne",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            preview.ingredients.forEach { ing ->
                                Text(
                                    "• ${ing.name} — ${quantityLabel(ing.quantity, ing.unit, ing.freeUnitLabel, ing.unspecified)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            preview.warnings.forEach { warning ->
                                Text(
                                    warning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
