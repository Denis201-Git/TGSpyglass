package com.example.tgparser.data

import android.util.Log
import androidx.room.withTransaction
import com.example.tgparser.data.database.AppDatabaseProvider
import com.example.tgparser.data.database.entity.MediaEntity
import com.example.tgparser.data.database.entity.MessageEntity
import com.example.tgparser.data.database.relation.MessageWithMedia
import com.example.tgparser.model.Media
import com.example.tgparser.model.Message
import com.example.tgparser.model.ParserMessage
import com.example.tgparser.model.toMedia
import com.example.tgparser.utils.currentIsoTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map

object MessageRepository {

    private val database
        get() = AppDatabaseProvider.get()

    private val messageDao
        get() = database.messageDao()

    private val mediaDao
        get() = database.mediaDao()

    fun observeMessages(
        channelTelegramId: String
    ): Flow<List<Message>> {

        return messageDao
            .observeLatestMessages(channelTelegramId)
            .map { messagesWithMedia ->
                messagesWithMedia.map { it.toModel() }
            }
    }

    fun observeAllMessageVersions(
        channelTelegramId: String
    ): Flow<List<Message>> {

        return messageDao
            .observeAllMessages(channelTelegramId)
            .map { messagesWithMedia ->
                messagesWithMedia.map { it.toModel() }
            }
    }

    fun observeMessageVersions(
        channelTelegramId: String,
        messageId: Int
    ): Flow<List<Message>> {

        return messageDao
            .observeMessageVersions(
                channelTelegramId = channelTelegramId,
                messageId = messageId
            )
            .map { messagesWithMedia ->
                messagesWithMedia.map { it.toModel() }
            }
    }

    suspend fun getMessages(
        channelTelegramId: String
    ): List<Message> {

        return messageDao
            .getLatestMessages(channelTelegramId)
            .map { it.toModel() }
    }

    suspend fun getMessageVersions(
        channelTelegramId: String,
        messageId: Int
    ): List<Message> {

        return messageDao
            .observeMessageVersions(
                channelTelegramId = channelTelegramId,
                messageId = messageId
            )
            .map { messagesWithMedia ->
                messagesWithMedia.map { it.toModel() }
            }
            .first()
    }

    suspend fun deleteMessages(
        channelTelegramId: String
    ) {
        messageDao.deleteForChannel(channelTelegramId)
    }

    suspend fun getMessageVersion(
        channelTelegramId: String,
        messageId: Int,
        version: Int
    ): Message? {

        return messageDao
            .getMessageVersion(
                channelTelegramId = channelTelegramId,
                messageId = messageId,
                version = version
            )
            ?.toModel()
    }

    suspend fun getLatestMessage(
        channelTelegramId: String,
        messageId: Int
    ): Message? {

        return messageDao
            .getLatestMessage(
                channelTelegramId = channelTelegramId,
                messageId = messageId
            )
            ?.toModel()
    }

    suspend fun syncMessages(
        channelTelegramId: String,
        parserMessages: List<ParserMessage>
    ) {
        database.withTransaction {
            syncMessagesInternal(
                channelTelegramId = channelTelegramId,
                parserMessages = parserMessages
            )
        }
    }

    internal suspend fun syncMessagesInternal(
        channelTelegramId: String,
        parserMessages: List<ParserMessage>
    ) {

        val parserIds = parserMessages
            .map { it.messageId }
            .toSet()

        for (parser in parserMessages) {

            val currentWithMedia = messageDao.getLatestMessage(
                channelTelegramId = channelTelegramId,
                messageId = parser.messageId
            )

            if (currentWithMedia == null) {

                val message = createNewMessage(
                    channelTelegramId = channelTelegramId,
                    parser = parser
                )

                insertMessage(message)

                continue
            }

            val current = currentWithMedia.toModel()

            var currentEntity = currentWithMedia.message

            if (current.isDeleted) {

                currentEntity = currentEntity.copy(
                    isDeleted = false
                )

                messageDao.update(currentEntity)
            }

            if (contentChanged(current, parser)) {

                currentEntity = currentEntity.copy(
                    isEdited = true
                )

                messageDao.update(currentEntity)

                val nextVersion = createNextVersion(
                    current = current,
                    parser = parser
                )

                insertMessage(nextVersion)

                continue
            }

            if (viewsChanged(current, parser)) {

                currentEntity = currentEntity.copy(
                    views = parser.views
                )

                messageDao.update(currentEntity)
            }
        }

        val currentMessages = messageDao.getLatestMessages(
            channelTelegramId = channelTelegramId
        )

        currentMessages.forEach { messageWithMedia ->

            val message = messageWithMedia.toModel()

            if (
                !message.isDeleted &&
                message.messageId !in parserIds
            ) {
                markDeleted(messageWithMedia.message)
            }
        }
    }

    private suspend fun insertMessage(
        message: Message
    ) {

        val messageEntity = message.toEntity()

        val messageDatabaseId = messageDao.insert(
            messageEntity
        )

        val mediaEntities = message.media.map { media ->

            media.toEntity(
                messageId = messageDatabaseId
            )

        }

        if (mediaEntities.isNotEmpty()) {
            mediaDao.insertAll(
                mediaEntities
            )
        }
    }

    private fun createNewMessage(
        channelTelegramId: String,
        parser: ParserMessage
    ): Message {

        return Message(
            channelTelegramId = channelTelegramId,
            messageId = parser.messageId,
            version = 1,
            text = parser.text,
            publishedAt = parser.publishedAt,
            savedAt = currentIsoTimestamp(),
            views = parser.views,
            media = parser.media.map { it.toMedia() },
            author = parser.author,
            url = parser.url
        )
    }

    private fun contentChanged(
        current: Message,
        parser: ParserMessage
    ): Boolean {

        return current.text != parser.text ||
                current.media != parser.media.map { it.toMedia() } ||
                current.author != parser.author ||
                current.url != parser.url ||
                current.publishedAt != parser.publishedAt
    }

    private fun viewsChanged(
        current: Message,
        parser: ParserMessage
    ): Boolean {

        return current.views != parser.views
    }

    private fun createNextVersion(
        current: Message,
        parser: ParserMessage
    ): Message {

        return Message(
            channelTelegramId = current.channelTelegramId,
            messageId = current.messageId,
            version = current.version + 1,
            text = parser.text,
            publishedAt = parser.publishedAt,
            savedAt = currentIsoTimestamp(),
            isEdited = false,
            isDeleted = false,
            views = parser.views,
            media = parser.media.map { it.toMedia() },
            author = parser.author,
            url = parser.url
        )
    }

    private suspend fun markDeleted(
        message: MessageEntity
    ) {

        messageDao.update(
            message.copy(
                isDeleted = true
            )
        )

        Log.d(
            "TGParser",
            "Message ${message.messageId} marked as deleted"
        )
    }

    private fun Message.toEntity(): MessageEntity {

        return MessageEntity(
            id = id,
            channelTelegramId = channelTelegramId,
            messageId = messageId,
            version = version,
            text = text,
            publishedAt = publishedAt,
            savedAt = savedAt,
            isEdited = isEdited,
            isDeleted = isDeleted,
            views = views,
            author = author,
            url = url
        )
    }

    private fun Media.toEntity(
        messageId: Long
    ): MediaEntity {

        return MediaEntity(
            messageId = messageId,
            type = type,
            url = url,
            thumbUrl = thumbUrl,
            duration = duration,
            title = title,
            description = description
        )
    }

    private fun MessageWithMedia.toModel(): Message {

        return Message(
            id = message.id,
            channelTelegramId = message.channelTelegramId,
            messageId = message.messageId,
            version = message.version,
            text = message.text,
            publishedAt = message.publishedAt,
            savedAt = message.savedAt,
            isEdited = message.isEdited,
            isDeleted = message.isDeleted,
            views = message.views,
            media = media.map { mediaEntity ->
                Media(
                    type = mediaEntity.type,
                    url = mediaEntity.url,
                    thumbUrl = mediaEntity.thumbUrl,
                    duration = mediaEntity.duration,
                    title = mediaEntity.title,
                    description = mediaEntity.description
                )
            },
            author = message.author,
            url = message.url
        )
    }
}