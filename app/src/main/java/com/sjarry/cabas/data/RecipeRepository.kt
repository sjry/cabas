package com.sjarry.cabas.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sjarry.cabas.data.dao.RecipeWithDetails
import com.sjarry.cabas.data.entities.IngredientEntity
import com.sjarry.cabas.data.entities.RecipeEntity
import com.sjarry.cabas.data.entities.StepEntity
import com.sjarry.cabas.parser.ParsedRecipe
import com.sjarry.cabas.parser.RecipeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Bilan d'un import, affiché à l'utilisateur juste après l'opération. */
data class ImportReport(
    val imported: Int = 0,
    val updated: Int = 0,
    val removed: Int = 0,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
) {
    val touched: Int get() = imported + updated

    fun summary(): String {
        if (errors.isNotEmpty() && touched == 0 && removed == 0) {
            return errors.first()
        }
        val parts = mutableListOf<String>()
        if (imported > 0) parts += "$imported recette${s(imported)} importée${s(imported)}"
        if (updated > 0) parts += "$updated mise${s(updated)} à jour"
        if (removed > 0) parts += "$removed supprimée${s(removed)}"
        if (parts.isEmpty()) parts += "Aucune recette importée"
        val suffix = buildString {
            if (warnings.isNotEmpty()) append(" · ${warnings.size} avertissement${s(warnings.size)}")
            if (errors.isNotEmpty()) append(" · ${errors.size} erreur${s(errors.size)}")
        }
        return parts.joinToString(", ") + suffix
    }

    private fun s(n: Int) = if (n > 1) "s" else ""
}

class RecipeRepository(
    private val context: Context,
    private val db: AppDatabase,
) {
    private val recipeDao = db.recipeDao()

    fun observeRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeAll()

    /** Recettes réduites à ce que le sélecteur du menu doit chercher : titre et ingrédients. */
    fun observeCandidates(): Flow<List<RecipeCandidate>> = recipeDao.observeCandidates()

    fun observeRecipe(id: Long): Flow<RecipeWithDetails?> = recipeDao.observeWithDetails(id)

    suspend fun deleteRecipe(id: Long) = withContext(Dispatchers.IO) {
        recipeDao.deleteRecipe(id)
    }

    /** Import depuis le sélecteur de fichiers. */
    suspend fun importFromUris(uris: List<Uri>): ImportReport = withContext(Dispatchers.IO) {
        var report = ImportReport()
        for (uri in uris) {
            report = try {
                val name = displayName(uri)
                val text = readText(uri)
                val parsed = RecipeParser.parse(text, fallbackTitle = name.removeMarkdownExtension())
                val created = upsert(parsed, text, uri.toString(), name)
                report.copy(
                    imported = report.imported + if (created) 1 else 0,
                    updated = report.updated + if (created) 0 else 1,
                    warnings = report.warnings + parsed.warnings.prefixed(parsed.title),
                )
            } catch (e: Exception) {
                report.copy(errors = report.errors + "Lecture impossible : ${e.message ?: uri.lastPathSegment}")
            }
        }
        report
    }

    /** Import d'un markdown collé à la main : pas d'URI, dédoublonnage par titre. */
    suspend fun importFromText(markdown: String): ImportReport = withContext(Dispatchers.IO) {
        if (markdown.isBlank()) return@withContext ImportReport(errors = listOf("Le texte est vide."))
        val parsed = RecipeParser.parse(markdown, fallbackTitle = "Recette collée")
        val created = upsert(parsed, markdown, sourceUri = null, fileName = null)
        ImportReport(
            imported = if (created) 1 else 0,
            updated = if (created) 0 else 1,
            warnings = parsed.warnings.prefixed(parsed.title),
        )
    }

    /**
     * Re-synchronise un dossier : (ré)importe tous les `.md` qu'il contient et
     * supprime les recettes dont le fichier a disparu.
     */
    suspend fun syncFolder(treeUri: Uri): ImportReport = withContext(Dispatchers.IO) {
        val tree = DocumentFile.fromTreeUri(context, treeUri)
            ?: return@withContext ImportReport(errors = listOf("Dossier introuvable."))

        var report = ImportReport()
        val seen = mutableSetOf<String>()

        for (file in tree.listFiles()) {
            val name = file.name ?: continue
            if (!file.isFile || !name.isMarkdown()) continue
            seen += file.uri.toString()
            report = try {
                val text = readText(file.uri)
                val parsed = RecipeParser.parse(text, fallbackTitle = name.removeMarkdownExtension())
                val created = upsert(parsed, text, file.uri.toString(), name)
                report.copy(
                    imported = report.imported + if (created) 1 else 0,
                    updated = report.updated + if (created) 0 else 1,
                    warnings = report.warnings + parsed.warnings.prefixed(parsed.title),
                )
            } catch (e: Exception) {
                report.copy(errors = report.errors + "$name : ${e.message ?: "lecture impossible"}")
            }
        }

        // Les recettes issues de ce dossier dont le fichier n'existe plus sont retirées.
        val prefix = "$treeUri/document/"
        val obsolete = recipeDao.allSourceUris()
            .filterNotNull()
            .filter { it.startsWith(prefix) && it !in seen }
        if (obsolete.isNotEmpty()) {
            recipeDao.deleteBySourceUris(obsolete)
            report = report.copy(removed = obsolete.size)
        }
        report
    }

    /** @return true si la recette a été créée, false si une recette existante a été mise à jour. */
    private suspend fun upsert(
        parsed: ParsedRecipe,
        rawMarkdown: String,
        sourceUri: String?,
        fileName: String?,
    ): Boolean {
        val now = System.currentTimeMillis()
        val existing = when {
            sourceUri != null -> recipeDao.findBySourceUri(sourceUri)
            else -> recipeDao.findPastedByTitle(parsed.title)
        }

        val recipeId: Long
        val created: Boolean
        if (existing == null) {
            recipeId = recipeDao.insertRecipe(
                RecipeEntity(
                    title = parsed.title,
                    sourceUri = sourceUri,
                    sourceFileName = fileName,
                    rawMarkdown = rawMarkdown,
                    importedAt = now,
                ),
            )
            created = true
        } else {
            recipeId = existing.id
            recipeDao.updateRecipe(recipeId, parsed.title, rawMarkdown, fileName, now)
            recipeDao.deleteIngredientsOf(recipeId)
            recipeDao.deleteStepsOf(recipeId)
            created = false
        }

        recipeDao.insertIngredients(
            parsed.ingredients.mapIndexed { index, ing ->
                IngredientEntity(
                    recipeId = recipeId,
                    name = ing.name,
                    quantity = ing.quantity,
                    unit = ing.unit,
                    freeUnitLabel = ing.freeUnitLabel,
                    unspecified = ing.unspecified,
                    rawLine = ing.rawLine,
                    position = index,
                )
            },
        )
        recipeDao.insertSteps(
            parsed.steps.mapIndexed { index, text ->
                StepEntity(recipeId = recipeId, text = text, position = index)
            },
        )
        return created
    }

    private fun readText(uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalStateException("fichier illisible")

    private fun displayName(uri: Uri): String =
        DocumentFile.fromSingleUri(context, uri)?.name
            ?: uri.lastPathSegment?.substringAfterLast('/')
            ?: "recette.md"
}

private fun String.isMarkdown(): Boolean =
    endsWith(".md", ignoreCase = true) ||
        endsWith(".markdown", ignoreCase = true) ||
        endsWith(".txt", ignoreCase = true)

private fun String.removeMarkdownExtension(): String =
    substringBeforeLast('.').ifBlank { this }

private fun List<String>.prefixed(title: String): List<String> = map { "$title — $it" }
