package io.github.denis201git.tgspyglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.denis201git.tgspyglass.data.preferences.ThemePreferences
import io.github.denis201git.tgspyglass.ui.theme.AppTheme
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