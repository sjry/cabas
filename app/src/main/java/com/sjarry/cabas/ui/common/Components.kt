package com.sjarry.cabas.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Écran vide explicatif, réutilisé par les trois onglets. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        action?.let {
            // Centré : un slot peut empiler plusieurs boutons de largeurs différentes.
            Column(
                modifier = Modifier.padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { it() }
        }
    }
}

/** Rend une ligne de liste cliquable sans répéter le modifier partout. */
fun Modifier.clickableListItem(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)

/**
 * Ligne de liste avec une action secondaire sur appui long. L'appui court reste
 * obligatoire — `combinedClickable` l'exige — donc toute ligne qui écoute l'appui
 * long doit aussi faire quelque chose de l'appui court.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.clickableListItem(onClick: () -> Unit, onLongClick: () -> Unit): Modifier =
    this.combinedClickable(onClick = onClick, onLongClick = onLongClick)

/**
 * Puces de filtre à choix unique ; ré-appuyer sur la puce active l'éteint. Pas de puce
 * « Toutes » : l'absence de sélection le dit déjà. L'appelant ne passe que les valeurs
 * présentes dans sa liste — une puce qui ne rendrait rien est du bruit.
 */
@Composable
fun <T> SingleChoiceChipRow(
    items: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    /** Marges intérieures de la rangée : nulles dans un dialogue, qui a déjà les siennes. */
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            val isSelected = item == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) null else item) },
                label = { Text(label(item)) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}
