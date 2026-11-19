package io.github.denis201git.tgspyglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.denis201git.tgspyglass.data.preferences.LanguagePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class LanguageViewModel(
    application: Application
) : AndroidViewModel(application) {


    private val preferences =
        LanguagePreferences(application)


    val language =
        preferences.language.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "en"
        )


    fun setLanguage(
        language: String
    ) {

        viewModelScope.launch {

            preferences.setLanguage(
                language
            )

        }

    }

}