/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.dto

data class AuthResponse (
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val username: String? = null,
    val role: String? = null,
    val message: String? = null
)
