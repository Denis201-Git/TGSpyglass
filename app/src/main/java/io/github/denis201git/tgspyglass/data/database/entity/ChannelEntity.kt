package io.github.denis201git.tgspyglass.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels"
)
data class ChannelEntity(

    @PrimaryKey
    val telegramId: String,

    val name: String,

    val lastMessage: String,

    val messagesCount: Int,

    val lastUpdate: String,

    val lastRefreshTime: String
)