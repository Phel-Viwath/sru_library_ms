/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.analytic

data class BookEachCollege(
    val collegeName: String,
    val bookEachLanguage: Map<String, Int>
)
