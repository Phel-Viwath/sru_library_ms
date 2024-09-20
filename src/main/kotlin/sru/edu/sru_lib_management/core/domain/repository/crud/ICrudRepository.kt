/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository.crud

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface ICrudRepository<T, ID> {
    suspend fun save(entity: T): T
    suspend fun update(entity: T): T
    suspend fun getById(id: ID): T?
    fun getAll(): Flow<T>
    suspend fun delete(id: ID): Boolean
}

