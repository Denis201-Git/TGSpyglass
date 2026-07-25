package com.example.tgparser.utils

private const val TELEGRAM_CHANNEL_PREFIX = "https://t.me/s/"

private val allowedCharacters =
    Regex("^[A-Za-z0-9_@:/.]+$")

fun formatTelegramLink(input: String): String? {

    val value = input.trim()

    if (!allowedCharacters.matches(value)) {
        return null
    }

    val username = value
        .removePrefix("@")
        .removePrefix("https://t.me/s/")
        .removePrefix("http://t.me/s/")
        .removePrefix("https://t.me/")
        .removePrefix("http://t.me/")
        .removePrefix("https://telegram.me/")
        .removePrefix("http://telegram.me/")
        .trim('/')

    if (username.isEmpty()) {
        return null
    }

    return TELEGRAM_CHANNEL_PREFIX + username

}