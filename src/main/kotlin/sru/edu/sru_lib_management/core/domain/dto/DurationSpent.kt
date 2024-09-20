/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

data class DurationSpent(
    val studentId: Long,
    val studentName: String,
    val major: String,
    val degree: String,
    val generation: Int,
    val totalTimeSpent: Float
)
