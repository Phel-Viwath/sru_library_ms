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
    val visitorId: Long?,
    @field:JsonFormat(pattern = "HH:mm:ss")
    val entryTimes: LocalTime,
    @field:JsonFormat(pattern = "HH:mm:ss")
    val exitTimes: LocalTime?,
    val purpose: String,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val attendDate: LocalDate,
)
