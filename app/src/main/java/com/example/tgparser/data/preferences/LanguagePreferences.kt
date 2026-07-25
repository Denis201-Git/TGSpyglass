package com.example.tgparser.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(
    name = "language_preferences"
)

class LanguagePreferences(
    private val context: Context
) {

    companion object {

        private val LANGUAGE_KEY =
            stringPreferencesKey("language")

    }


    val language =
        context.languageDataStore.data.map { preferences ->

            preferences[LANGUAGE_KEY]
                ?: "en"

        }


    suspend fun setLanguage(
        language: String
    ) {

        context.languageDataStore.edit { preferences ->

            preferences[LANGUAGE_KEY] = language

        }

    }

}