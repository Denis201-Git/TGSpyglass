package com.example.tgparser.model

fun ParserMessage.toMessage(
    savedAt: String
): Message {

    return Message(

        channelUsername = channelUsername,

        messageId = messageId,

        text = text,

        publishedAt = publishedAt,

        savedAt = savedAt,

        views = views,

        media = media.map { it.toMedia() },

        author = author,

        url = url

    )

}

fun ParserMedia.toMedia(): Media {

    return Media(

        type = type,

        url = url,

        thumbUrl = thumbUrl,

        duration = duration,

        title = title,

        description = description

    )

}