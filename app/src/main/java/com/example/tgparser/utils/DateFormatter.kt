package com.example.tgparser.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun formatMessageDate(
    date: String
): String {

    return try {

        val instant = Instant.parse(date)

        val dateTime =
            instant
                .atZone(
                    ZoneId.systemDefault()
                )


        val formatter =
            DateTimeFormatter.ofPattern(
                "HH:mm"
            )


        formatter.format(dateTime)


    } catch (e: Exception) {

        date

    }

}