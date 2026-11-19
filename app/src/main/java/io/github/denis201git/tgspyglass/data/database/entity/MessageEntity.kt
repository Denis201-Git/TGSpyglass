package io.github.denis201git.tgspyglass.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["telegramId"],
            childColumns = ["channelTelegramId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["channelTelegramId", "messageId", "version"],
            unique = true
        )
    ]
)
data class MessageEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val channelTelegramId: String,

    val messageId: Int,

    val version: Int,

    val text: String,

    val publishedAt: String,

    val savedAt: String,

    val isEdited: Boolean,

    val isDeleted: Boolean,

    val views: String,

    val author: String,

    val url: String,

    val links: String = ""
)
