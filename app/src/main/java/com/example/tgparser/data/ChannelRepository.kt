package com.example.tgparser.data

import androidx.compose.runtime.mutableStateListOf
import com.example.tgparser.model.Channel

object ChannelRepository {

    private val channels = mutableStateListOf<Channel>()

    fun getChannels(): List<Channel> {
        return channels
    }

    fun addChannel(channel: Channel) {
        channels.add(channel)
    }

    fun updateChannel(updatedChannel: Channel) {
        val index = channels.indexOfFirst { it.telegramId == updatedChannel.telegramId }

        if (index != -1) {
            channels[index] = updatedChannel
        } else {
            channels.add(updatedChannel)
        }
    }

    fun deleteChannel(channelTelegramId: String) {

        MessageRepository.deleteMessages(channelTelegramId)

        channels.removeAll {
            it.telegramId == channelTelegramId
        }
    }
}