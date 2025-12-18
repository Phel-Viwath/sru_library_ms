/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.attend

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalTime

data class AttendDto(
    val studentId: Long? = null,
    val sruStaffId: String? = null,
    @field:JsonFormat(pattern = "HH:mm:ss")
    val entryTimes: LocalTime,
    @field:JsonFormat(pattern = "HH:mm:ss")
    val exitingTimes: LocalTime?,
    val purpose: String,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val attendDate: LocalDate,
)