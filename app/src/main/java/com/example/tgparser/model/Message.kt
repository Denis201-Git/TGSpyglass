package com.example.tgparser.model

data class Message(

    val id: Long = 0L,

    val channelTelegramId: String,

    val messageId: Int,

    val version: Int = 1,

    val text: String,

    val publishedAt: String,

    val savedAt: String,

    val isEdited: Boolean = false,

    val isDeleted: Boolean = false,

    val views: String,

    val media: List<Media>,

    val author: String,

    val url: String

)

data class Media(

    val type: String,

    val url: String,

    val thumbUrl: String? = null,

    val duration: String? = null,

    val title: String? = null,

    val description: String? = null,

    val notSupported: Boolean = false

)