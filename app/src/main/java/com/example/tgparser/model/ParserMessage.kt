package com.example.tgparser.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParserMessage(

    @SerialName("channel")
    val channelUsername: String,

    @SerialName("message_id")
    val messageId: Int,

    @SerialName("text")
    val text: String,

    @SerialName("date")
    val publishedAt: String,

    @SerialName("views")
    val views: String,

    @SerialName("media")
    val media: List<ParserMedia>,

    @SerialName("author")
    val author: String,

    @SerialName("url")
    val url: String

)

@Serializable
data class ParserMedia(

    @SerialName("type")
    val type: String,

    @SerialName("url")
    val url: String,

    @SerialName("thumb_url")
    val thumbUrl: String? = null,

    @SerialName("duration")
    val duration: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("description")
    val description: String? = null

)