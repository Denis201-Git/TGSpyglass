package com.example.tgparser.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tgparser.data.database.entity.MediaEntity
import com.example.tgparser.data.database.entity.MessageEntity

data class MessageWithMedia(

    @Embedded
    val message: MessageEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val media: List<MediaEntity>
)