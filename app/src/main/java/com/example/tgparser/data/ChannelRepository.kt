package com.example.tgparser.data

import com.example.tgparser.data.database.AppDatabaseProvider
import com.example.tgparser.data.database.entity.ChannelEntity
import com.example.tgparser.model.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ChannelRepository {

    private val channelDao
        get() = AppDatabaseProvider
            .get()
            .channelDao()

    fun observeChannels(): Flow<List<Channel>> {
        return channelDao
            .observeAll()
            .map { entities ->
                entities.map { it.toModel() }
            }
    }

    suspend fun getChannels(): List<Channel> {
        return channelDao
            .getAll()
            .map { it.toModel() }
    }

    suspend fun getChannel(
        telegramId: String
    ): Channel? {

        return channelDao
            .getByTelegramId(telegramId)
            ?.toModel()
    }

    suspend fun addChannel(
        channel: Channel
    ) {
        channelDao.insert(
            channel.toEntity()
        )
    }

    suspend fun updateChannel(
        updatedChannel: Channel
    ) {
        val existing = channelDao.getByTelegramId(
            updatedChannel.telegramId
        )

        if (existing == null) {
            channelDao.insert(
                updatedChannel.toEntity()
            )
        } else {
            channelDao.update(
                updatedChannel.toEntity()
            )
        }
    }

    suspend fun deleteChannel(
        channelTelegramId: String
    ) {
        val channel = channelDao.getByTelegramId(
            channelTelegramId
        ) ?: return

        channelDao.delete(channel)
    }

    private fun Channel.toEntity(): ChannelEntity {
        return ChannelEntity(
            telegramId = telegramId,
            name = name,
            lastMessage = lastMessage,
            messagesCount = messagesCount,
            lastUpdate = lastUpdate,
            lastRefreshTime = lastRefreshTime
        )
    }

    private fun ChannelEntity.toModel(): Channel {
        return Channel(
            telegramId = telegramId,
            name = name,
            lastMessage = lastMessage,
            messagesCount = messagesCount,
            lastUpdate = lastUpdate,
            lastRefreshTime = lastRefreshTime
        )
    }
}