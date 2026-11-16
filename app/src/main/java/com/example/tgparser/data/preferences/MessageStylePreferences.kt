package com.example.tgparser.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.messageStyleDataStore by preferencesDataStore(
    name = "message_style_preferences"
)

enum class MessageStyle {
    CLASSIC,
    BORDERLESS
}

class MessageStylePreferences(
    private val context: Context
) {

    companion object {

        private val STYLE_KEY =
            stringPreferencesKey("message_style")

        private const val CLASSIC_VALUE = "classic"
        private const val BORDERLESS_VALUE = "borderless"

    }

    val style =
        context.messageStyleDataStore.data.map { preferences ->

            when (preferences[STYLE_KEY]) {

                BORDERLESS_VALUE -> MessageStyle.BORDERLESS

                else -> MessageStyle.CLASSIC

            }

        }

    suspend fun setStyle(
        style: MessageStyle
    ) {

        context.messageStyleDataStore.edit { preferences ->

            preferences[STYLE_KEY] = when (style) {

                MessageStyle.CLASSIC -> CLASSIC_VALUE

                MessageStyle.BORDERLESS -> BORDERLESS_VALUE

            }

        }

    }

}
