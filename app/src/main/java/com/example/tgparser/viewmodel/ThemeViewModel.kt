package com.example.tgparser.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tgparser.data.preferences.ThemePreferences
import com.example.tgparser.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ThemeViewModel(
    application: Application
) : AndroidViewModel(application) {


    private val preferences =
        ThemePreferences(application)


    val theme =
        preferences.theme.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppTheme.SYSTEM
        )


    fun setTheme(
        theme: AppTheme
    ) {

        viewModelScope.launch {

            preferences.setTheme(theme)

        }

    }

}