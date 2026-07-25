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
import com.example.tgparser.model.Channel
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import com.example.tgparser.data.AppSnackbarManager
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    onChannelClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    scope: CoroutineScope
) {

    val strings = LocalStrings.current


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


    val channels = ChannelRepository.getChannels()

    val scope = rememberCoroutineScope()


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
                            if (ParserState.isBusy) {
                                scope.launch {
                                    AppSnackbarManager.show(strings.parserBusy)
                                }
                                return@FloatingActionButton
                            }

                            ParserState.lock()
                            scope.launch {
                                try {
                                    ChannelUpdater.updateAllChannels()
                                } finally {
                                    ParserState.unlock()
                                }
                            }
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


            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)

            ) {


                items(channels) { channel ->


                    ChannelCard(

                        channel = channel,

                        onClick = {

                            onChannelClick(
                                channel.id
                            )

                        }

                    )


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
                            },
                            onChannelAdded = { channel ->
                                ChannelRepository.addChannel(channel)
                                animateAddChannelDialog = false
                            },
                            scope = scope
                        )


                    }


                }


            }


        }


    }

}