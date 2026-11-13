package com.example.tgparser.utils

private const val TELEGRAM_CHANNEL_PREFIX = "https://t.me/s/"

private val usernameRegex = Regex("^[A-Za-z0-9_]{1,64}$")

private val reservedPaths = setOf(
    "s",
    "c",
    "joinchat",
    "addstickers",
    "share",
    "iv",
    "proxy",
    "socks",
    "login",
    "auth",
    "confirmphone",
    "setlanguage",
    "addtheme",
    "contact"
)

private val telegramPrefixes = listOf(
    "https://t.me/s/",
    "http://t.me/s/",
    "https://www.t.me/s/",
    "http://www.t.me/s/",
    "https://t.me/",
    "http://t.me/",
    "https://www.t.me/",
    "http://www.t.me/",
    "https://telegram.me/s/",
    "http://telegram.me/s/",
    "https://telegram.me/",
    "http://telegram.me/",
    "t.me/s/",
    "t.me/",
    "telegram.me/s/",
    "telegram.me/",
    "www.t.me/s/",
    "www.t.me/",
    "@"
)

fun formatTelegramLink(input: String): String? {

    var value = input.trim()

    if (value.isEmpty()) {
        return null
    }

    value = value
        .substringBefore('?')
        .substringBefore('#')
        .trimEnd('/')

    if (value.isEmpty()) {
        return null
    }

    val lowerValue = value.lowercase()

    val prefix = telegramPrefixes.firstOrNull { lowerValue.startsWith(it) }

    if (prefix != null) {
        value = value.substring(prefix.length)
    }

    val username = value.substringBefore('/').trim()

    if (!usernameRegex.matches(username)) {
        return null
    }

    if (username.lowercase() in reservedPaths) {
        return null
    }

    return TELEGRAM_CHANNEL_PREFIX + username
}
