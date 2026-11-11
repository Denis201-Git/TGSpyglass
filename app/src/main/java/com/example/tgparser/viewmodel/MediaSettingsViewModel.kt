package com.example.tgparser.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tgparser.data.preferences.MediaPreferences
import com.example.tgparser.data.preferences.MediaSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MediaSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {


    private val preferences =
        MediaPreferences(application)


    val settings =
        preferences.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MediaSettings()
        )


    fun setPhotos(
        enabled: Boolean
    ) {

        viewModelScope.launch {

            preferences.setPhotos(enabled)

        }

    }


    fun setVideos(
        enabled: Boolean
    ) {

        viewModelScope.launch {

            preferences.setVideos(enabled)

        }

    }


    fun setFiles(
        enabled: Boolean
    ) {

        viewModelScope.launch {

            preferences.setFiles(enabled)

        }

    }

}
