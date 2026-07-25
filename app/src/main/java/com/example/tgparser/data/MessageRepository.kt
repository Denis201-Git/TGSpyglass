package com.example.tgparser.data

import androidx.compose.runtime.mutableStateMapOf
import com.example.tgparser.model.Message

object MessageRepository {

    private val messages = mutableStateMapOf<String, List<Message>>()

    fun getMessages(channelId: String): List<Message> {
        return messages[channelId] ?: emptyList()
    }

    fun addMessages(channelId: String, newMessages: List<Message>) {
        val current = messages[channelId].orEmpty()
        messages[channelId] = (current + newMessages).sortedBy { it.messageId }
    }

    fun replaceMessages(channelId: String, newMessages: List<Message>) {
        messages[channelId] = newMessages.sortedBy { it.messageId }
    }

}