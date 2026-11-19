package io.github.denis201git.tgspyglass.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParseResult(

    @SerialName("error")
    val error: String = "",

    @SerialName("channel")
    val channel: ChannelInfo = ChannelInfo(),

    @SerialName("messages")
    val messages: List<ParserMessage> = emptyList()

)