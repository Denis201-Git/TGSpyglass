package com.example.tgparser.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tgparser.data.database.entity.MessageEntity
import com.example.tgparser.data.database.relation.MessageWithMedia
import androidx.paging.PagingSource

@Dao
interface MessageDao {

    @Query("""
    SELECT *
    FROM messages
    WHERE channelTelegramId = :channelTelegramId
    AND version = (
        SELECT MAX(versions.version)
        FROM messages AS versions
        WHERE versions.channelTelegramId = messages.channelTelegramId
        AND versions.messageId = messages.messageId
    )
    ORDER BY messageId DESC
""")
    fun pagingLatestMessages(
        channelTelegramId: String
    ): PagingSource<Int, MessageWithMedia>

    @Query("""
    SELECT *
    FROM messages
    WHERE channelTelegramId = :channelTelegramId
    AND version = (
        SELECT MAX(versions.version)
        FROM messages AS versions
        WHERE versions.channelTelegramId = messages.channelTelegramId
        AND versions.messageId = messages.messageId
    )
    ORDER BY messageId
""")
    suspend fun getLatestMessages(
        channelTelegramId: String
    ): List<MessageWithMedia>

    @Query("""
    SELECT *
    FROM messages
    WHERE channelTelegramId = :channelTelegramId
    AND messageId BETWEEN :minMessageId AND :maxMessageId
    AND version = (
        SELECT MAX(versions.version)
        FROM messages AS versions
        WHERE versions.channelTelegramId = messages.channelTelegramId
        AND versions.messageId = messages.messageId
    )
""")
    suspend fun getLatestMessagesInRange(
        channelTelegramId: String,
        minMessageId: Int,
        maxMessageId: Int
    ): List<MessageEntity>

    @Query("""
    SELECT *
    FROM messages
    WHERE channelTelegramId = :channelTelegramId
    AND version = (
        SELECT MAX(versions.version)
        FROM messages AS versions
        WHERE versions.channelTelegramId = messages.channelTelegramId
        AND versions.messageId = messages.messageId
    )
    ORDER BY messageId DESC
    LIMIT 1
""")
    suspend fun getLatestChannelMessage(
        channelTelegramId: String
    ): MessageEntity?

    @Query("""
    SELECT MAX(messageId)
    FROM messages
    WHERE channelTelegramId = :channelTelegramId
""")
    suspend fun getMaxMessageId(
        channelTelegramId: String
    ): Int?

    @Query("""
    SELECT COUNT(DISTINCT messageId)
    FROM messages
    WHERE channelTelegramId = :channelTelegramId
""")
    suspend fun countMessages(
        channelTelegramId: String
    ): Int

    @Query("""
        SELECT *
        FROM messages
        WHERE channelTelegramId = :channelTelegramId
        AND messageId = :messageId
        ORDER BY version
    """)
    suspend fun getMessageVersions(
        channelTelegramId: String,
        messageId: Int
    ): List<MessageWithMedia>

    @Query("""
        SELECT *
        FROM messages
        WHERE channelTelegramId = :channelTelegramId
        AND messageId = :messageId
        ORDER BY version DESC
        LIMIT 1
    """)
    suspend fun getLatestMessage(
        channelTelegramId: String,
        messageId: Int
    ): MessageWithMedia?

    @Query("""
        SELECT *
        FROM messages
        WHERE channelTelegramId = :channelTelegramId
        AND messageId = :messageId
        AND version = :version
        LIMIT 1
    """)
    suspend fun getMessageVersion(
        channelTelegramId: String,
        messageId: Int,
        version: Int
    ): MessageWithMedia?

    @Query("""
        SELECT COUNT(*)
        FROM messages
        WHERE channelTelegramId = :channelTelegramId
        AND messageId = :messageId
    """)
    suspend fun countVersions(
        channelTelegramId: String,
        messageId: Int
    ): Int

    @Insert(
        onConflict = OnConflictStrategy.ABORT
    )
    suspend fun insert(
        message: MessageEntity
    ): Long

    @Update
    suspend fun update(
        message: MessageEntity
    )

    @Query("""
        DELETE FROM messages
        WHERE channelTelegramId = :channelTelegramId
    """)
    suspend fun deleteForChannel(
        channelTelegramId: String
    )
}