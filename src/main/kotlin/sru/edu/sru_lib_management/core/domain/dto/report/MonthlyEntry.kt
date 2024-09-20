/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.report

data class MonthlyEntry(
    val time: String,
    val month: String,
    val entry: Map<String, Int>
)
