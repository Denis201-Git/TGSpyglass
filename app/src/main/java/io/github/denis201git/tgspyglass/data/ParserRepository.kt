package io.github.denis201git.tgspyglass.data

import android.util.Log
import io.github.denis201git.tgspyglass.model.ParseResult
import kotlinx.serialization.json.Json
import mobile.Parser

class ParserRepository {

    private val parser = Parser()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseChannel(
        url: String,
        latestKnownMessageId: Int = 0,
        pageLimit: Int = 0
    ): ParseResult {

        val rawResult = parser.parseChannelIncremental(
            url,
            latestKnownMessageId.toLong(),
            pageLimit.toLong()
        )

        val result = json.decodeFromString<ParseResult>(rawResult)

        Log.d(
            "TGParser",
            "Parsed $url: ${result.messages.size} messages, error=${result.error}"
        )

        return result
    }

    companion object {
        const val INITIAL_PARSE_PAGES = 50
    }
}