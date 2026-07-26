package com.example.tgparser.data

import android.util.Log
import com.example.tgparser.utils.currentIsoTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.tgparser.model.toMessage

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

        val savedAt = currentIsoTimestamp()

        val messages = result.messages.map {
            it.toMessage(savedAt)
        }

        if (result.error.isNotEmpty()) {
            Log.d("TGParser", "Update failed for $channelId: ${result.error}")

            return@withContext
        }

        val lastMessage = messages.maxByOrNull { it.messageId }

        MessageRepository.replaceMessages(channel.telegramId, messages)

        ChannelRepository.updateChannel(
            channel.copy(
                name = result.channel.name.ifBlank { channel.name },
                username = result.channel.username.ifBlank { channel.username },
                lastMessage = lastMessage?.text.orEmpty(),
                messagesCount = result.messages.size,
                lastUpdate = lastMessage?.publishedAt.orEmpty(),
                lastRefreshTime = currentIsoTimestamp()
            )
        )

        Log.d("TGParser", "Updated $channelId: ${messages.size} messages")    }

}