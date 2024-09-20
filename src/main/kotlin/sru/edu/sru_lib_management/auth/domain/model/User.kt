/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.model

data class User(
    val email: String,
    val username: String,
    val password: String,
    val roles: Role
)