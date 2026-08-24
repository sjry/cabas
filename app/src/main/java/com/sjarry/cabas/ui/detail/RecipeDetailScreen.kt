package com.sjarry.cabas.ui.detail

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sjarry.cabas.ui.AppViewModelProvider
import com.sjarry.cabas.ui.common.EmptyState
import com.sjarry.cabas.ui.common.quantityLabel
import com.sjarry.cabas.ui.common.servingsLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val details by viewModel.recipe.collectAsStateWithLifecycle()
    // Les ingrédients sont stockés pour 1 personne : on les multiplie par les
    // convives choisis dans le menu, comme le fait la liste de courses.
    val servings = viewModel.servings

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(details?.recipe?.title ?: "Recette") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        val recipe = details
        if (recipe == null) {
            EmptyState(
                icon = Icons.Filled.Description,
                title = "Recette introuvable",
                message = "Elle a peut-être été supprimée.",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text(
                    "Ingrédients — pour ${servingsLabel(servings)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                )
            }

            items(recipe.ingredients, key = { "ingredient-${it.id}" }) { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(ingredient.name, modifier = Modifier.weight(1f))
                    Text(
                        text = quantityLabel(
                            ingredient.quantity * servings,
                            ingredient.unit,
                            ingredient.freeUnitLabel,
                            ingredient.unspecified,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (recipe.steps.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        "Étapes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    )
                }
                itemsIndexed(recipe.steps, key = { _, step -> "step-${step.id}" }) { index, step ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(
                            text = "${index + 1}.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(28.dp),
                        )
                        Text(step.text)
                    }
                }
            }

            if (recipe.ingredients.isEmpty() && recipe.steps.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cette recette ne contient ni ingrédient ni étape reconnus.")
                    }
                }
            }
        }
    }
}
