package com.example.tgparser.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tgparser.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(
    name = "settings"
)


class ThemePreferences(
    private val context: Context
) {

    private val themeKey = stringPreferencesKey("theme")


    val theme: Flow<AppTheme> = context.dataStore.data.map { preferences ->

        when (
            preferences[themeKey]
        ) {

            "LIGHT" -> AppTheme.LIGHT
            "DARK" -> AppTheme.DARK
            "AMOLED" -> AppTheme.AMOLED

            else -> AppTheme.SYSTEM

        }

    }


    suspend fun setTheme(
        theme: AppTheme
    ) {

        context.dataStore.edit { preferences ->

            preferences[themeKey] = theme.name

        }

    }

}