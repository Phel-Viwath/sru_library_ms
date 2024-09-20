/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.dashbord

data class StudentEntry(
    val no: Int,
    val studentName: String,
    val major: String,
    val year: Int,
    val entryTime: String,
    val purpose: String,
    val status: String
)
