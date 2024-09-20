/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.repository

import sru.edu.sru_lib_management.auth.domain.dto.UserDto
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User

interface AuthRepository<T> {
    suspend fun save(entity: T)
    suspend fun update(entity: T): Boolean
    suspend fun findByEmail(email: String): User?
    suspend fun changeRole(email: String, role: Role): Boolean
    suspend fun getAll(): List<UserDto>

    suspend fun getRole(email: String): Role
}