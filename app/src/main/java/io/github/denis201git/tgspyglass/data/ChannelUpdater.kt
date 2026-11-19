package io.github.denis201git.tgspyglass.data

import android.util.Log
import io.github.denis201git.tgspyglass.utils.currentIsoTimestamp
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

        val latestKnownMessageId = MessageRepository
            .getMaxMessageId(channel.telegramId)
            ?: 0

        val result = parserRepository.parseChannel(
            url = url,
            latestKnownMessageId = latestKnownMessageId
        )

        if (result.error.isNotEmpty()) {
            Log.d("TGParser", "Update failed for $channelId: ${result.error}")

            return@withContext
        }

        MessageRepository.syncMessages(
            channel.telegramId,
            result.messages
        )

        val lastMessage = MessageRepository.getLatestChannelMessage(
            channel.telegramId
        )

        val messagesCount = MessageRepository.countMessages(
            channel.telegramId
        )

        ChannelRepository.updateChannel(
            channel.copy(
                name = result.channel.name.ifBlank { channel.name },
                lastMessage = lastMessage?.text.orEmpty(),
                messagesCount = messagesCount,
                lastUpdate = lastMessage?.publishedAt.orEmpty(),
                lastRefreshTime = currentIsoTimestamp()
            )
        )

        Log.d(
            "TGParser",
            "Updated $channelId: parsed ${result.messages.size}, stored $messagesCount messages"
        )
    }
}