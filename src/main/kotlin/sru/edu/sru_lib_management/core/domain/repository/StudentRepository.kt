/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.core.domain.dto.StudentDto
import sru.edu.sru_lib_management.core.domain.model.Students
import sru.edu.sru_lib_management.core.domain.repository.crud.ICrudRepository

@Repository
interface StudentRepository : ICrudRepository<Students, Long>{
    fun getStudentDetail(): Flow<StudentDto>
    suspend fun getStudentDetailById(studentId: Long): StudentDto?
}
