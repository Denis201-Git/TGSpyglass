package com.example.tgparser.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tgparser.ui.localization.LocalStrings
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator
import com.example.tgparser.utils.formatTelegramLink
import androidx.compose.runtime.rememberCoroutineScope
import com.example.tgparser.data.MessageRepository
import com.example.tgparser.data.ParserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.tgparser.model.Channel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.example.tgparser.utils.formatMessageDate
import android.util.Log
import com.example.tgparser.data.ParserState
import com.example.tgparser.data.AppSnackbarManager
import kotlinx.coroutines.CoroutineScope

@Composable
fun AddChannelDialog(
    onDismiss: () -> Unit,
    onChannelAdded: (Channel) -> Unit,
    scope: CoroutineScope
) {

    val strings = LocalStrings.current

    val repository = remember { ParserRepository() }

    val focusManager = LocalFocusManager.current

    var isLoading by remember {
        mutableStateOf(false)
    }

    var statusMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isError by remember {
        mutableStateOf(false)
    }

    var channelInput by remember {

        mutableStateOf("")

    }

    fun addChannel() {

        val url = formatTelegramLink(channelInput)

        if (url == null) {
            isLoading = false
            isError = true
            statusMessage = strings.addChannelError
            return
        }

        if (ParserState.isBusy) {
            scope.launch {
                AppSnackbarManager.show(strings.parserBusy)
            }
            return
        }

        ParserState.lock()
        statusMessage = null
        isLoading = true

        scope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    repository.parseChannel(url)
                }

                if (result.error.isNotEmpty()) {

                    isError = true
                    statusMessage = strings.addChannelError

                } else {

                    val lastMessage = result.messages.maxByOrNull { it.messageId }

                    val channel = Channel(
                        id = result.channel.id,
                        telegramId = result.channel.id,
                        name = result.channel.name,
                        username = "@${result.channel.id}",
                        lastMessage = lastMessage?.text.orEmpty(),
                        messagesCount = result.messages.size,
                        lastUpdate = formatMessageDate(lastMessage?.date ?: "")
                    )

                    MessageRepository.addMessages(
                        channel.telegramId,
                        result.messages
                    )

                    onChannelAdded(channel)

                    isError = false
                    statusMessage = strings.addChannelSuccess

                    focusManager.clearFocus()
                }

            } catch (e: Exception) {

                isError = true
                statusMessage = strings.addChannelError

            } finally {

                isLoading = false
                ParserState.unlock()
            }
        }
    }

    Card(

        modifier = Modifier
            .fillMaxWidth(0.9f)
            .widthIn(max = 500.dp),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )

    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.SpaceBetween

        ) {

            Text(

                text = strings.addChannel,

                style = MaterialTheme.typography.titleLarge

            )


            IconButton(

                onClick = onDismiss

            ) {

                Icon(

                    imageVector = Icons.Default.Close,

                    contentDescription = null

                )

            }

        }

        HorizontalDivider()


        Column(

            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 20.dp
                ),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {


            OutlinedTextField(

                value = channelInput,

                onValueChange = {
                    channelInput = it
                    statusMessage = null
                },

                modifier = Modifier.fillMaxWidth(),

                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),

                keyboardActions = KeyboardActions(
                    onDone = {
                        addChannel()
                    }
                ),

                label = {

                    Text(strings.usernameOrLink)

                },

                placeholder = {

                    Text("@telegram")

                }

            )

            statusMessage?.let {

                Text(
                    text = it,
                    color = if (isError) Color.Red else Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodySmall
                )

            }


            Button(

                onClick = {
                    addChannel()
                },

                modifier = Modifier.fillMaxWidth(),

                enabled = channelInput.isNotBlank() && !isLoading

            ) {

                if (isLoading) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )

                        Text("Adding...")
                    }

                } else {

                    Text("Add")

                }

            }


        }
    }
}