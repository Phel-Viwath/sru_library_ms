/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.core.domain.model.Students
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.StudentDto

@Service
interface StudentService {
    suspend fun saveStudent(students: Students): CoreResult<Students?>
    suspend fun updateStudent(studentId: Long, students: Students): CoreResult<Students?>
    suspend fun deleteStudent(studentId: Long): CoreResult<Boolean>
    fun getStudents(): Flow<Students>
    suspend fun getStudent(studentId: Long): CoreResult<StudentDto?>
    fun getAllStudentDetail(): Flow<StudentDto>
}
