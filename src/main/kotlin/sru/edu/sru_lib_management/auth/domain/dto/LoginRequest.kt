/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.dto

data class LoginRequest(
    val email: String,
    val password: String
)
