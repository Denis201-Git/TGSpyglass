package io.github.denis201git.tgspyglass.data

import io.github.denis201git.tgspyglass.data.database.AppDatabaseProvider
import io.github.denis201git.tgspyglass.data.database.entity.ChannelEntity
import io.github.denis201git.tgspyglass.model.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import io.github.denis201git.tgspyglass.model.ParserMessage

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

    suspend fun addChannelWithMessages(
        channel: Channel,
        parserMessages: List<ParserMessage>
    ) {
        val database = AppDatabaseProvider.get()

        database.withTransaction {

            channelDao.insert(
                channel.toEntity()
            )

            MessageRepository.syncMessagesInternal(
                channelTelegramId = channel.telegramId,
                parserMessages = parserMessages
            )
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