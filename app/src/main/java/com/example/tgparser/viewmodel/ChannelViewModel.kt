package com.example.tgparser.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.tgparser.data.ChannelUpdater
import com.example.tgparser.data.MessageRepository
import com.example.tgparser.data.ParserState
import kotlinx.coroutines.launch

class ChannelViewModel(
    application: Application,
    val channelId: String
) : AndroidViewModel(application) {

    val messages = MessageRepository
        .getMessagesPager(channelId)
        .cachedIn(viewModelScope)

    val displayedVersions = mutableStateMapOf<Int, Int>()

    fun onVersionChange(messageId: Int, version: Int) {
        displayedVersions[messageId] = version
    }

    fun refresh(onDone: () -> Unit = {}) {

        if (ParserState.isBusy) {
            onDone()
            return
        }

        viewModelScope.launch {
            ParserState.lock()
            try {
                ChannelUpdater.updateChannel(channelId)
            } finally {
                ParserState.unlock()
                onDone()
            }
        }
    }
}

class ChannelViewModelFactory(
    private val application: Application,
    private val channelId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ChannelViewModel(
            application = application,
            channelId = channelId
        ) as T
    }
}
