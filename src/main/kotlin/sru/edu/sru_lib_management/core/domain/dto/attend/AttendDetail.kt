/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.attend

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalTime

data class AttendDetail(
    val id: String,
    val name: String,
    @JsonFormat(pattern = "HH:mm:ss")
    val entryTimes: LocalTime,
    @JsonFormat(pattern = "HH:mm:ss")
    val exitingTime: LocalTime?,
    val purpose: String,
    var status: String?
)
