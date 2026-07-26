package com.example.tgparser.model

data class Channel(
    val telegramId: String,
    val name: String,
    val username: String,
    val lastMessage: String,
    val messagesCount: Int,
    val lastUpdate: String,
    val lastRefreshTime: String = ""
)