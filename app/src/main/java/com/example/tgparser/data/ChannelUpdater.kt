package com.example.tgparser.data

import android.util.Log
import com.example.tgparser.utils.formatMessageDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChannelUpdater {

    private val parserRepository = ParserRepository()

    suspend fun updateAllChannels() = withContext(Dispatchers.IO) {
        val channels = ChannelRepository.getChannels().toList()

        for (channel in channels) {
            updateChannel(channel.telegramId)
        }
    }

    suspend fun updateChannel(channelId: String) = withContext(Dispatchers.IO) {
        val channel = ChannelRepository.getChannels()
            .firstOrNull { it.telegramId == channelId }
            ?: return@withContext

        val url = "https://t.me/s/${channel.telegramId}"
        val result = parserRepository.parseChannel(url)

        if (result.error.isNotEmpty()) {
            Log.d("TGParser", "Update failed for $channelId: ${result.error}")
            return@withContext
        }

        val lastMessage = result.messages.maxByOrNull { it.messageId }

        MessageRepository.replaceMessages(channel.telegramId, result.messages)

        ChannelRepository.updateChannel(
            channel.copy(
                name = result.channel.name.ifBlank { channel.name },
                username = result.channel.username.ifBlank { channel.username },
                lastMessage = lastMessage?.text.orEmpty(),
                messagesCount = result.messages.size,
                lastUpdate = formatMessageDate(lastMessage?.date ?: "")
            )
        )

        Log.d("TGParser", "Updated $channelId: ${result.messages.size} messages")
    }

}