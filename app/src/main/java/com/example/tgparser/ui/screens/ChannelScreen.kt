package com.example.tgparser.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.tgparser.ui.components.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.tgparser.data.AppSnackbarManager
import com.example.tgparser.data.MessageRepository
import com.example.tgparser.data.ParserState
import com.example.tgparser.data.preferences.MediaSettings
import com.example.tgparser.model.Message
import com.example.tgparser.ui.components.EmptyMessages
import com.example.tgparser.ui.components.MessageCard
import com.example.tgparser.ui.localization.LocalStrings
import com.example.tgparser.viewmodel.ChannelViewModel
import com.example.tgparser.viewmodel.ChannelViewModelFactory
import com.example.tgparser.viewmodel.MediaSettingsViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    channelId: String,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val viewModel: ChannelViewModel = viewModel(
        factory = ChannelViewModelFactory(
            application = context.applicationContext as android.app.Application,
            channelId = channelId
        )
    )

    val lazyMessages = viewModel.messages.collectAsLazyPagingItems()
    val strings = LocalStrings.current

    val mediaSettingsViewModel: MediaSettingsViewModel = viewModel()
    val mediaSettings by mediaSettingsViewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun refreshChannel() {
        if (ParserState.isBusy) {
            scope.launch {
                AppSnackbarManager.show(
                    strings.parserBusy,
                    isError = true
                )
            }
            return
        }
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = channelId)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = AppIcons.ArrowBack(),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshChannel() }) {
                        Icon(
                            imageVector = AppIcons.Refresh(),
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        when {
            lazyMessages.loadState.refresh is LoadState.Loading &&
                    lazyMessages.itemCount == 0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            lazyMessages.loadState.refresh is LoadState.Error -> {
                val error = (lazyMessages.loadState.refresh as LoadState.Error).error

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = error.message ?: "Error")
                        Button(
                            onClick = { lazyMessages.retry() },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(text = "Retry")
                        }
                    }
                }
            }

            lazyMessages.itemCount == 0 -> {
                EmptyMessages()
            }

            else -> {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(
                        count = lazyMessages.itemCount,
                        key = lazyMessages.itemKey { it.id },
                        contentType = lazyMessages.itemContentType { "message" }
                    ) { index ->
                        val latestMessage = lazyMessages[index]
                        if (latestMessage != null) {
                            VersionedMessageCard(
                                channelId = channelId,
                                latestMessage = latestMessage,
                                mediaSettings = mediaSettings,
                                displayedVersion = viewModel.displayedVersions[latestMessage.messageId],
                                onVersionChange = { version ->
                                    viewModel.onVersionChange(
                                        latestMessage.messageId,
                                        version
                                    )
                                }
                            )
                        }
                    }

                    if (lazyMessages.loadState.append is LoadState.Loading) {
                        item(key = "append-loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    if (lazyMessages.loadState.prepend is LoadState.Loading) {
                        item(key = "prepend-loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionedMessageCard(
    channelId: String,
    latestMessage: Message,
    mediaSettings: MediaSettings,
    displayedVersion: Int?,
    onVersionChange: (Int) -> Unit
) {

    val versionsCount by remember(latestMessage.messageId, latestMessage.version) {
        flow {
            emit(
                MessageRepository.countVersions(
                    channelTelegramId = channelId,
                    messageId = latestMessage.messageId
                )
            )
        }
    }.collectAsState(initial = 1)

    val message by remember(displayedVersion, latestMessage.version) {
        flow {
            if (displayedVersion == null) {
                emit(latestMessage)
            } else {
                emit(
                    MessageRepository.getMessageVersion(
                        channelTelegramId = channelId,
                        messageId = latestMessage.messageId,
                        version = displayedVersion
                    ) ?: latestMessage
                )
            }
        }
    }.collectAsState(initial = latestMessage)

    MessageCard(
        message = message,
        versionsCount = versionsCount,
        mediaSettings = mediaSettings,
        onPreviousVersion = {
            if (message.version > 1) {
                onVersionChange(message.version - 1)
            }
        },
        onNextVersion = {
            if (message.isEdited) {
                onVersionChange(message.version + 1)
            }
        }
    )
}
