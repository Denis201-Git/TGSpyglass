package com.example.tgparser.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.mediaDataStore by preferencesDataStore(
    name = "media_preferences"
)

data class MediaSettings(
    val photos: Boolean = true,
    val videos: Boolean = true,
    val files: Boolean = true
) {

    fun isEnabled(type: String): Boolean {

        return when (type) {

            "photo" -> photos

            "video" -> videos

            "file", "document" -> files

            else -> true

        }

    }

}

class MediaPreferences(
    private val context: Context
) {

    companion object {

        private val PHOTOS_KEY =
            booleanPreferencesKey("photos")

        private val VIDEOS_KEY =
            booleanPreferencesKey("videos")

        private val FILES_KEY =
            booleanPreferencesKey("files")

    }


    val settings =
        context.mediaDataStore.data.map { preferences ->

            MediaSettings(
                photos = preferences[PHOTOS_KEY] ?: true,
                videos = preferences[VIDEOS_KEY] ?: true,
                files = preferences[FILES_KEY] ?: true
            )

        }


    suspend fun setPhotos(
        enabled: Boolean
    ) {

        context.mediaDataStore.edit { preferences ->

            preferences[PHOTOS_KEY] = enabled

        }

    }


    suspend fun setVideos(
        enabled: Boolean
    ) {

        context.mediaDataStore.edit { preferences ->

            preferences[VIDEOS_KEY] = enabled

        }

    }


    suspend fun setFiles(
        enabled: Boolean
    ) {

        context.mediaDataStore.edit { preferences ->

            preferences[FILES_KEY] = enabled

        }

    }

}
