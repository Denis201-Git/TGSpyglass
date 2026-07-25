package com.example.tgparser.model

data class Channel(
    val id: String,
    val telegramId: String,
    val name: String,
    val username: String,
    val lastMessage: String,
    val messagesCount: Int,
    val lastUpdate: String
)