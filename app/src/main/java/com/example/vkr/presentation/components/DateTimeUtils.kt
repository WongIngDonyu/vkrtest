package com.example.vkr.ui.components

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")

    fun parseDisplayFormatted(input: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(input, DISPLAY_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDisplay(datetime: LocalDateTime): String {
        return datetime.format(DISPLAY_FORMATTER)
    }
}