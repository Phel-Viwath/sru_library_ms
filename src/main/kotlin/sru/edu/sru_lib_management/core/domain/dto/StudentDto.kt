/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class StudentDto(
    val studentId: Long?,
    val studentName: String,
    val gender: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate,
    val degreeLevel: String,
    val majorName: String,
    val generation: Int
)