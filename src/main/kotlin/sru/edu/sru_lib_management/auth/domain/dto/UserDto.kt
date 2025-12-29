/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.dto

import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User

data class UserDto(
    val userId: String? = null,
    val username: String,
    val email: String,
    val role: Role
)

fun User.toUserDto(): UserDto = UserDto(
    userId = userId,
    email = email,
    role = roles,
    username = username
)