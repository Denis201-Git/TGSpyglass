package io.github.denis201git.tgspyglass.model

fun ParserMessage.toMessage(
    savedAt: String
): Message {

    return Message(

        channelTelegramId = channelUsername,

        messageId = messageId,

        text = text,

        links = links.map { it.toTextLink() },

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

        description = description,

        notSupported = notSupported

    )

}
fun ParserLink.toTextLink(): TextLink {

    return TextLink(
        start = start,
        length = length,
        url = url
    )

}
