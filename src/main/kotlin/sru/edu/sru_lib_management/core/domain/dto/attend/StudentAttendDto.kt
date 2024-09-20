/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.attend

import com.fasterxml.jackson.annotation.JsonFormat
import sru.edu.sru_lib_management.core.domain.model.Attend
import java.time.LocalDate
import java.time.LocalTime

data class StudentAttendDto (
    val attendId: Long,
    val studentId: Long,
    @JsonFormat(pattern = "HH:mm:ss") val entryTimes: LocalTime,
    @JsonFormat(pattern = "HH:mm:ss") val exitingTimes: LocalTime?,
    val purpose: String,
    @JsonFormat(pattern = "yyyy:MM:dd") val date: LocalDate
)


