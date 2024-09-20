/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.analytic

data class TotalStudentAttendByTime(
    val totalAttend: Int,
    val totalFemale: Int,
    val morning: Int,
    val afternoon: Int,
    val evening: Int
)
