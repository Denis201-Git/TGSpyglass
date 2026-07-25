package com.example.tgparser.data

import android.util.Log
import com.example.tgparser.model.ParseResult
import kotlinx.serialization.json.Json
import mobile.Parser

class ParserRepository {

    private val parser = Parser()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseChannel(url: String): ParseResult {

        val result = parser.parseChannel(url)

        Log.d("TGParser", "URL: $url")
        Log.d("TGParser", "RESULT: $result")

        return json.decodeFromString(result)
    }

}