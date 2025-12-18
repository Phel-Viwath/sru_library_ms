/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Students (
    val studentId: Long?,
    val studentName: String,
    val gender: String,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate,
    val degreeLevelId: String,
    val majorId: String,
    val generation: Int
)
