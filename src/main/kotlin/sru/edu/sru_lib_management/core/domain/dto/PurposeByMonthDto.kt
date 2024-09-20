/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.YearMonth

data class PurposeByMonthDto(
    val other: Int,
    val reading: Int,
    val assignment: Int,
    val usePc: Int,
    @JsonFormat(pattern = "yyyy-MM") val month: YearMonth
)
