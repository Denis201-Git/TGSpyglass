package com.example.tgparser.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val timeFormatter =
    DateTimeFormatter.ofPattern(
        "HH:mm"
    )

private val monthDayFormatter =
    DateTimeFormatter.ofPattern(
        "MMM d",
        Locale.ENGLISH
    )

private val monthDayYearFormatter =
    DateTimeFormatter.ofPattern(
        "MMM d, yyyy",
        Locale.ENGLISH
    )

private fun parseDate(
    date: String
): java.time.ZonedDateTime? {

    return try {

        Instant
            .parse(date)
            .atZone(
                ZoneId.systemDefault()
            )

    } catch (e: Exception) {

        null

    }

}

/**

 * Relative date for channel cards.
 *
 * Examples:
 * 13:42
 * Yesterday
 * Mon
 * Jul 9
 * 1 year ago
 * 2 years ago
 */
fun formatRelativeDate(
    date: String
): String {

    val dateTime = parseDate(date)
        ?: return date

    val today = LocalDate.now(
        ZoneId.systemDefault()
    )

    val messageDate = dateTime.toLocalDate()

    val daysAgo =
        ChronoUnit.DAYS.between(
            messageDate,
            today
        )

    return when {
            daysAgo == 0L -> {
            timeFormatter.format(dateTime)
        }

        daysAgo == 1L -> {
            "Yesterday"
        }

        daysAgo in 2L..6L -> {
            dateTime.dayOfWeek
                .getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    Locale.ENGLISH
                )
        }

        daysAgo < 365L -> {
            monthDayFormatter.format(dateTime)
        }

        else -> {

            val years =
                ChronoUnit.YEARS.between(
                    messageDate,
                    today
                )

            if (years == 1L) {
                "1 year ago"
            } else {
                "$years years ago"
            }

        }

    }

}

/**

 * Date and time for message cards.
 *
 * Examples:
 * 13:42
 * Yesterday 13:42
 * Mon 13:42
 * Jul 9, 13:42
 * Jul 9, 2025, 13:42
 */
fun formatMessageDate(
    date: String
): String {

    val dateTime = parseDate(date)
        ?: return date

    val today = LocalDate.now(
        ZoneId.systemDefault()
    )

    val messageDate = dateTime.toLocalDate()

    val daysAgo =
        ChronoUnit.DAYS.between(
            messageDate,
            today
        )

    val time =
        timeFormatter.format(dateTime)

    return when {
            daysAgo == 0L -> {
            time
        }

        daysAgo == 1L -> {
            "Yesterday $time"
        }

        daysAgo in 2L..6L -> {

            val dayOfWeek =
                dateTime.dayOfWeek
                    .getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        Locale.ENGLISH
                    )

            "$dayOfWeek $time"
        }

        messageDate.year != today.year -> {
            "${monthDayYearFormatter.format(dateTime)}, $time"
        }

        else -> {
            "${monthDayFormatter.format(dateTime)}, $time"
        }

    }

}

fun currentIsoTimestamp(): String {
    return Instant.now().toString()
}

fun formatCurrentTime(): String {
    return try {

        val dateTime =
            java.time.LocalDateTime.now()
                .atZone(
                    ZoneId.systemDefault()
                )

        timeFormatter.format(dateTime)

    } catch (_: Exception) {

        ""

    }
}
