package com.example.tgparser.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tgparser.model.Message
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.tgparser.ui.components.MessageCard
import com.example.tgparser.data.MessageRepository
import com.example.tgparser.ui.components.EmptyMessages
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.tgparser.data.ChannelUpdater
import com.example.tgparser.data.ParserState
import com.example.tgparser.data.AppSnackbarManager
import com.example.tgparser.ui.localization.LocalStrings
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    channelId: String,
    onBackClick: () -> Unit
) {

    val messages = MessageRepository.getMessages(channelId)

    val displayedVersions = remember {
        mutableStateMapOf<Int, Int>()
    }

    val strings = LocalStrings.current

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    fun refreshChannel() {
        if (ParserState.isBusy) {
            scope.launch {
                AppSnackbarManager.show(strings.parserBusy)
            }
            return
        }

        scope.launch {
            ParserState.lock()
            try {
                ChannelUpdater.updateChannel(channelId)
            } finally {
                ParserState.unlock()
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = channelId
                    )

                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )

                    }

                },

                actions = {
                    IconButton(
                        onClick = {
                            refreshChannel()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }

            )

        }

    ) { innerPadding ->


        if (messages.isEmpty()) {

            EmptyMessages()

        } else {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(messages) { latestMessage ->

                    val versions = MessageRepository.getMessageVersions(
                        channelId,
                        latestMessage.messageId
                    )

                    val displayedVersion =
                        displayedVersions[latestMessage.messageId]

                    val message =
                        if (displayedVersion == null) {

                            versions.last()

                        } else {

                            versions.firstOrNull {
                                it.version == displayedVersion
                            } ?: versions.last()

                        }

                    val versionsCount = versions.size

                    MessageCard(

                        message = message,

                        versionsCount = versionsCount,

                        onPreviousVersion = {

                            if (message.version > 1) {

                                displayedVersions[message.messageId] =
                                    message.version - 1

                            }

                        },

                        onNextVersion = {

                            if (message.isEdited) {

                                displayedVersions[message.messageId] =
                                    message.version + 1

                            }

                        }

                    )

                }

            }

        }

    }

}