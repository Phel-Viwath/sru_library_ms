/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.dto

import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User

data class UserDto(
    val username: String,
    val email: String,
    val role: Role
)

fun User.toUserDto(): UserDto = UserDto(
    email = email,
    role = roles,
    username = username
)