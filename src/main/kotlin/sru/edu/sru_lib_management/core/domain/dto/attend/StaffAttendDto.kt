/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.attend

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalTime

data class StaffAttendDto(
    val attendId: Long,
    val staffId: String,
    val staffName: String,
    val gender: String,
    @field:JsonFormat(pattern = "HH:mm:ss") val entryTimes: LocalTime,
    @field:JsonFormat(pattern = "HH:mm:ss") val exitingTimes: LocalTime?,
    val purpose: String,
    @field:JsonFormat(pattern = "yyyy:MM:dd")
    val date: LocalDate
)
