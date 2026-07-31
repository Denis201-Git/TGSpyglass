package com.example.tgparser.data

import android.util.Log
import com.example.tgparser.utils.currentIsoTimestamp
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

        MessageRepository.syncMessages(
            channel.telegramId,
            result.messages
        )

        val messages = MessageRepository.getMessages(channel.telegramId)

        val lastMessage = messages.maxByOrNull { it.messageId }

        ChannelRepository.updateChannel(
            channel.copy(
                name = result.channel.name.ifBlank { channel.name },
                lastMessage = lastMessage?.text.orEmpty(),
                messagesCount = result.messages.size,
                lastUpdate = lastMessage?.publishedAt.orEmpty(),
                lastRefreshTime = currentIsoTimestamp()
            )
        )

        Log.d("TGParser", "Updated $channelId: ${messages.size} messages")
    }
}