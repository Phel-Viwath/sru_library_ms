/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

data class BorrowDto(
    val bookId: String,
    val studentId: Long,
    val bookQuan: Int
)