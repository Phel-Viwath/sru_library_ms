/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.analytic

data class TotalBook (
    val totalBook: Int,
    val bookEachLanguage: Map<String, Int>
)