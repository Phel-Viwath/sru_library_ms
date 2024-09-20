/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

import java.time.LocalDate

data class MajorPurpose(
    val majorName: String,
    val purpose: String,
    val date: LocalDate
)