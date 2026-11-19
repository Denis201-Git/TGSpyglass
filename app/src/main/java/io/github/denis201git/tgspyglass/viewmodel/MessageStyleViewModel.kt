package io.github.denis201git.tgspyglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.denis201git.tgspyglass.data.preferences.MessageStyle
import io.github.denis201git.tgspyglass.data.preferences.MessageStylePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessageStyleViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferences =
        MessageStylePreferences(application)

    val style =
        preferences.style.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MessageStyle.CLASSIC
        )

    fun setStyle(
        style: MessageStyle
    ) {

        viewModelScope.launch {

            preferences.setStyle(style)

        }

    }

}
