/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

data class BookAvailableDto(
    val language: String,
    val totalBook: Int,
    val available: Int
)
