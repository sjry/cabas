package com.sjarry.cabas.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** Réglages persistés : pour l'instant, le dossier de recettes synchronisé. */
class SettingsStore(private val context: Context) {

    val folderUri: Flow<String?> = context.dataStore.data.map { it[FOLDER_URI] }

    suspend fun setFolderUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(FOLDER_URI) else prefs[FOLDER_URI] = uri
        }
    }

    private companion object {
        val FOLDER_URI = stringPreferencesKey("folder_uri")
    }
}
