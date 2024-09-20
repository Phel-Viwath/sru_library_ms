/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object IndochinaDateTime {
    private val indochinaZoneId = ZoneId.of("Asia/Phnom_Penh")

    fun indoChinaDate(): LocalDate {
        val indochinaDateTime = ZonedDateTime.now(indochinaZoneId)
        return indochinaDateTime.toLocalDate()
    }
    fun indoChinaTime(): LocalTime {
        val indochinaDateTime = ZonedDateTime.now(indochinaZoneId)
        val timeFormater = DateTimeFormatter.ofPattern("HH:mm:ss")
        return LocalTime.parse(indochinaDateTime.toLocalTime().format(timeFormater), timeFormater)
    }

    fun indoChinaDateTime(): LocalDateTime{
        return ZonedDateTime.now(indochinaZoneId).toLocalDateTime()
    }
}