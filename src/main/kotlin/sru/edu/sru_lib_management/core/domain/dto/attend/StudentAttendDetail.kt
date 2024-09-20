/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.attend

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalTime

data class StudentAttendDetail(
    val attendId: Long,
    val studentId: Long,
    val studentName: String,
    val gender: String,
    val major: String,
    val degreeLevel: String,
    val generation: Int,
    @JsonFormat(pattern = "HH:mm:ss")
    val entryTimes: LocalTime,
    @JsonFormat(pattern = "HH:mm:ss")
    val exitingTimes: LocalTime?,
    val purpose: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    var status: String?
)
