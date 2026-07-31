package com.example.tgparser.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.tgparser.model.Message
import com.example.tgparser.model.ParserMessage
import com.example.tgparser.model.toMedia
import com.example.tgparser.utils.currentIsoTimestamp

object MessageRepository {

    private val messages = mutableStateListOf<Message>()

    fun getMessages(
        channelTelegramId: String
    ): List<Message> {

        return messages

            .filter {
                it.channelTelegramId == channelTelegramId
            }

            .groupBy {
                it.messageId
            }

            .mapNotNull { (_, versions) ->
                versions.maxByOrNull { it.version }
            }

            .sortedBy {
                it.messageId
            }

    }

    fun getMessageVersions(
        channelTelegramId: String,
        messageId: Int
    ): List<Message> {

        return messages

            .filter {

                it.channelTelegramId == channelTelegramId &&
                        it.messageId == messageId

            }

            .sortedBy {
                it.version
            }

    }

    fun deleteMessages(channelTelegramId: String) {
        messages.removeAll {
            it.channelTelegramId == channelTelegramId
        }
    }

    fun getMessageVersion(
        channelTelegramId: String,
        messageId: Int,
        version: Int
    ): Message? {

        return messages.firstOrNull {

            it.channelTelegramId == channelTelegramId &&
                    it.messageId == messageId &&
                    it.version == version

        }

    }

    fun getLatestMessage(
        channelTelegramId: String,
        messageId: Int
    ): Message? {
        return latestMessage(channelTelegramId, messageId)
    }

    fun syncMessages(
        channelTelegramId: String,
        parserMessages: List<ParserMessage>
    ) {

        val parserIds = parserMessages.map { it.messageId }.toSet()

        for (parser in parserMessages) {

            val current = latestMessage(
                channelTelegramId,
                parser.messageId
            )

            // Новое сообщение
            if (current == null) {

                messages.add(
                    Message(
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
                )

                continue
            }

            if (current.isDeleted) {

                val index = messages.indexOf(current)

                if (index != -1) {
                    messages[index] = current.copy(
                        isDeleted = false
                    )
                }
            }

            if (contentChanged(current, parser)) {

                val currentIndex = messages.indexOf(current)

                if (currentIndex != -1) {
                    messages[currentIndex] = current.copy(
                        isEdited = true
                    )
                }

                messages.add(
                    createNextVersion(
                        current,
                        parser
                    )
                )

                continue
            }

            if (viewsChanged(current, parser)) {

                updateViews(
                    current,
                    parser
                )
            }
        }

        getMessages(channelTelegramId)
            .forEach { message ->

                if (
                    !message.isDeleted &&
                    message.messageId !in parserIds
                ) {
                    markDeleted(message)
                }
            }

        messages.sortWith(
            compareBy<Message>(
                { it.channelTelegramId },
                { it.messageId },
                { it.version }
            )
        )
    }

    private fun latestMessage(
        channelTelegramId: String,
        messageId: Int
    ): Message? {

        return messages

            .filter {

                it.channelTelegramId == channelTelegramId &&
                        it.messageId == messageId

            }

            .maxByOrNull {
                it.version
            }

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

    private fun updateViews(
        current: Message,
        parser: ParserMessage
    ) {

        val index = messages.indexOf(current)

        if (index == -1) return

        messages[index] = current.copy(
            views = parser.views
        )

    }

    private fun markDeleted(
        message: Message
    ) {

        val index = messages.indexOf(message)

        if (index == -1) return

        messages[index] = message.copy(
            isDeleted = true
        )

        Log.d(
            "TGParser",
            "Message ${message.messageId} marked as deleted"
        )

    }

}