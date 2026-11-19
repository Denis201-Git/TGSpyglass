package io.github.denis201git.tgspyglass.model

data class Channel(
    val telegramId: String,
    val name: String,
    val lastMessage: String,
    val messagesCount: Int,
    val lastUpdate: String,
    val lastRefreshTime: String = ""
) {
    val username: String
        get() = "@$telegramId"
}