package sru.edu.sru_lib_management.core.domain.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalTime

data class VisitorDetail(
    val attendId: Long?,
    val visitorId: Long?,
    val visitorName: String,
    val visitorType: VisitorType,
    @field:JsonFormat(pattern = "HH:mm:ss")
    val entryTimes: LocalTime,
    @field:JsonFormat(pattern = "HH:mm:ss")
    val exitTimes: LocalTime?,
    val purpose: String,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val attendDate: LocalDate,
)