/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.analytic

data class MostBorrow(
    var rank: Int?,
    val bookTitle: String,
    val genre: String,
    val borrowQuan: Int
)
