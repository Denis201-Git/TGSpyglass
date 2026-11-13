package com.example.tgparser.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["messageId"])
    ]
)
data class MediaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val messageId: Long,

    val type: String,

    val url: String,

    val thumbUrl: String?,

    val duration: String?,

    val title: String?,

    val description: String?,

    val notSupported: Boolean = false
)
