/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object OpeningTime {
    val SEVEN_AM: LocalTime = LocalTime.parse("07:00:00", DateTimeFormatter.ISO_LOCAL_TIME)
    val ELEVEN_AM: LocalTime = LocalTime.parse("11:00:00", DateTimeFormatter.ISO_LOCAL_TIME)
    val TWO_PM: LocalTime = LocalTime.parse("14:00:00", DateTimeFormatter.ISO_LOCAL_TIME)
    val FIVE_PM: LocalTime = LocalTime.parse("17:00:00", DateTimeFormatter.ISO_LOCAL_TIME)
    val FIVE_THIRTY_PM: LocalTime = LocalTime.parse("17:30:00", DateTimeFormatter.ISO_LOCAL_TIME)
    val SEVEN_THIRTY_PM: LocalTime = LocalTime.parse("19:30:00", DateTimeFormatter.ISO_LOCAL_TIME)
}