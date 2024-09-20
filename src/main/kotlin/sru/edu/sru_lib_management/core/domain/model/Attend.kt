/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalTime
import java.time.LocalDate

data class Attend (
    val attendId: Long?,
    val studentId: Long?,
    val staffId: String?,
    @JsonFormat(pattern = "HH:mm:ss")
    val entryTimes: LocalTime,
    @JsonFormat(pattern = "HH:mm:ss")
    val exitingTimes: LocalTime?,
    val purpose: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
)
