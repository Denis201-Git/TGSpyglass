package io.github.denis201git.tgspyglass.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import io.github.denis201git.tgspyglass.data.database.entity.MediaEntity
import io.github.denis201git.tgspyglass.data.database.entity.MessageEntity

data class MessageWithMedia(

    @Embedded
    val message: MessageEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val media: List<MediaEntity>
)