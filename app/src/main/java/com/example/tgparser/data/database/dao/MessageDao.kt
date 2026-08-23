package com.example.tgparser.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tgparser.data.database.entity.MessageEntity
import com.example.tgparser.data.database.relation.MessageWithMedia
import kotlinx.coroutines.flow.Flow
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
    ORDER BY messageId
""")
    fun pagingLatestMessages(
        channelTelegramId: String
    ): PagingSource<Int, MessageWithMedia>

    @Query("""
        SELECT *
        FROM messages
        WHERE channelTelegramId = :channelTelegramId
        AND version = (
            SELECT MAX(m2.version)
            FROM messages AS m2
            WHERE m2.channelTelegramId = messages.channelTelegramId
            AND m2.messageId = messages.messageId
        )
        ORDER BY messageId
    """)
    fun observeLatestMessages(
        channelTelegramId: String
    ): Flow<List<MessageWithMedia>>

    @Query("""
    SELECT *
    FROM messages
    WHERE channelTelegramId = :channelTelegramId
    ORDER BY messageId, version
""")
    fun observeAllMessages(
        channelTelegramId: String
    ): Flow<List<MessageWithMedia>>

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
        AND messageId = :messageId
        ORDER BY version
    """)
    fun observeMessageVersions(
        channelTelegramId: String,
        messageId: Int
    ): Flow<List<MessageWithMedia>>

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