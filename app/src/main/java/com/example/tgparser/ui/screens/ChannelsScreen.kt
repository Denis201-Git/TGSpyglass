package com.example.tgparser.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tgparser.ui.components.AddChannelDialog
import com.example.tgparser.ui.components.ChannelCard
import com.example.tgparser.ui.localization.LocalStrings
import kotlinx.coroutines.delay
import com.example.tgparser.data.ChannelRepository
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.example.tgparser.data.ParserState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.tgparser.data.ChannelUpdater
import androidx.compose.material.icons.outlined.Refresh
import com.example.tgparser.data.AppSnackbarManager
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    onChannelClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {

    val strings = LocalStrings.current

    var channelToDelete by remember {
        mutableStateOf<String?>(null)
    }


    var renderAddChannelDialog by remember {

        mutableStateOf(false)

    }


    var animateAddChannelDialog by remember {

        mutableStateOf(false)

    }


    LaunchedEffect(renderAddChannelDialog) {

        if (renderAddChannelDialog) {

            delay(10)

            animateAddChannelDialog = true

        }

    }


    LaunchedEffect(animateAddChannelDialog) {

        if (!animateAddChannelDialog) {

            delay(200)

            renderAddChannelDialog = false

        }

    }


    val channels by ChannelRepository
        .observeChannels()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    fun refreshAll() {
        if (ParserState.isBusy || isRefreshing) {
            if (ParserState.isBusy) {
                scope.launch {
                    AppSnackbarManager.show(strings.parserBusy)
                }
            }
            return
        }

        scope.launch {
            isRefreshing = true
            ParserState.lock()
            try {
                ChannelUpdater.updateAllChannels()
            } finally {
                ParserState.unlock()
                isRefreshing = false
            }
        }
    }

    fun refreshChannel(channelId: String) {
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


    Box(

        modifier = Modifier.fillMaxSize()

    ) {


        Scaffold(

            topBar = {

                TopAppBar(

                    modifier = Modifier.offset(y = (-16).dp),

                    title = {

                        Text(
                            strings.channels
                        )

                    },


                    actions = {

                        IconButton(

                            onClick = onSettingsClick

                        ) {

                            Icon(

                                imageVector = Icons.Outlined.Settings,

                                contentDescription = strings.settings

                            )

                        }

                    }

                )

            },


            floatingActionButton = {

                Column {

                    FloatingActionButton(
                        onClick = {
                            refreshAll()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    FloatingActionButton(

                        onClick = {
                            if (ParserState.isBusy) {
                                scope.launch {
                                    AppSnackbarManager.show(strings.parserBusy)
                                }
                                return@FloatingActionButton
                            }

                            renderAddChannelDialog = true
                        }

                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = strings.addChannel
                        )
                    }

                }

            }


        ) { innerPadding ->


            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { refreshAll() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(channels) { channel ->
                        ChannelCard(
                            channel = channel,
                            onClick = {
                                onChannelClick(channel.telegramId)
                            },
                            onRefreshClick = {
                                refreshChannel(channel.telegramId)
                            },
                            onDeleteClick = {
                                channelToDelete = channel.telegramId
                            }
                        )
                    }
                }
            }


        }



        if (renderAddChannelDialog) {


            AnimatedVisibility(

                visible = animateAddChannelDialog,


                enter = fadeIn(

                    animationSpec = tween(220)

                ),


                exit = fadeOut(

                    animationSpec = tween(180)

                )

            ) {


                Box(

                    modifier = Modifier
                        .fillMaxSize()

                ) {


                    Box(

                        modifier = Modifier
                            .fillMaxSize()
                            .background(

                                MaterialTheme.colorScheme.scrim.copy(
                                    alpha = 0.5f
                                )

                            )
                            .clickable(

                                indication = null,

                                interactionSource = remember {
                                    MutableInteractionSource()
                                }

                            ) {

                                animateAddChannelDialog = false

                            }

                    )


                    Box(

                        modifier = Modifier.fillMaxSize(),

                        contentAlignment = Alignment.Center

                    ) {


                        AddChannelDialog(
                            onDismiss = {
                                animateAddChannelDialog = false
                            }
                        )


                    }


                }


            }


        }

        if (channelToDelete != null) {

            AlertDialog(
                onDismissRequest = {
                    channelToDelete = null
                },

                title = {
                    Text("Delete channel?")
                },

                text = {
                    Text(
                        "The channel and all its messages will be deleted."
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            val channelId = channelToDelete

                            if (channelId != null) {
                                scope.launch {
                                    ChannelRepository.deleteChannel(channelId)
                                }
                            }

                            channelToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }

                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            channelToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }

                }
            )
        }

    }

}