package com.sjarry.cabas.ui.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sjarry.cabas.data.ImportReport

/**
 * Le détail d'un bilan d'import : les erreurs, puis les avertissements recette par recette.
 * Le snackbar n'en donne que le compte ; c'est ici qu'on apprend *quelle* ligne de *quel*
 * fichier n'a pas été comprise — indispensable maintenant que catégorie et saison s'écrivent à
 * la main.
 */
@Composable
fun ImportReportDialog(report: ImportReport, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bilan de l'import") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(report.summary(), style = MaterialTheme.typography.bodyMedium)

                report.errors.forEach { error ->
                    Text(
                        error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                report.warnings.groupBy { it.recipe }.forEach { (recipe, warnings) ->
                    Text(
                        recipe,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    warnings.forEach { warning ->
                        Text(
                            "• ${warning.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        },
    )
}
