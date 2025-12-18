/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Donation (
    val bookId: String,
    val donatorId: Int,
    @field:JsonFormat(pattern = "yyyy-MM-dd") val donateDate: LocalDate
)