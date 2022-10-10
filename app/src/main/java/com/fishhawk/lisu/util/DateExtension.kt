package com.fishhawk.lisu.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fishhawk.lisu.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
fun LocalDateTime.readableString(): String {
    val now = LocalDateTime.now()
    val days = ChronoUnit.DAYS.between(this, now)
    return when {
        days == 0L -> {
            when (val hours = ChronoUnit.HOURS.between(this, now)) {
                0L -> {
                    when (val minutes = ChronoUnit.MINUTES.between(this, now)) {
                        0L -> "just now"
                        else -> "$minutes minutes age"
                    }
                }
                else -> "$hours hours age"
            }
        }
        days == 1L -> stringResource(R.string.history_yesterday)
        days <= 5L -> stringResource(R.string.history_n_days_ago).format(days)
        else -> this.format(DateTimeFormatter.ofPattern(stringResource(R.string.history_date_format)))
    }
}

@Composable
fun LocalDate.readableString(): String {
    val now = LocalDate.now()
    val days = ChronoUnit.DAYS.between(this, now)
    return when {
        days == 0L -> stringResource(R.string.history_today)
        days == 1L -> stringResource(R.string.history_yesterday)
        days <= 5L -> stringResource(R.string.history_n_days_ago).format(days)
        else -> this.format(DateTimeFormatter.ofPattern(stringResource(R.string.history_date_format)))
    }
}