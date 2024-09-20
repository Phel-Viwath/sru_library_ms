/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.repository

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.*
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import sru.edu.sru_lib_management.core.data.query.StudentQuery.DELETE_STUDENT_QUERY
import sru.edu.sru_lib_management.core.data.query.StudentQuery.GET_STUDENTS_DETAIL_QUERY
import sru.edu.sru_lib_management.core.data.query.StudentQuery.GET_STUDENTS_QUERY
import sru.edu.sru_lib_management.core.data.query.StudentQuery.GET_STUDENT_DETAIL_QUERY
import sru.edu.sru_lib_management.core.data.query.StudentQuery.GET_STUDENT_QUERY
import sru.edu.sru_lib_management.core.data.query.StudentQuery.SAVE_STUDENT_QUERY
import sru.edu.sru_lib_management.core.data.query.StudentQuery.UPDATE_STUDENT_QUERY
import sru.edu.sru_lib_management.core.domain.dto.StudentDto
import sru.edu.sru_lib_management.core.domain.model.Students
import sru.edu.sru_lib_management.core.domain.repository.StudentRepository
import java.time.LocalDate

@Component
class StudentRepositoryImp (
    private val client: DatabaseClient
): StudentRepository {

    override fun getStudentDetail(): Flow<StudentDto> {
        return client.sql(GET_STUDENTS_DETAIL_QUERY)
            .map { row: Row, _ ->
                row.mapToStudentDto()
            }
            .all()
            .asFlow()
    }

    override suspend fun getStudentDetailById(studentId: Long): StudentDto? {
        return client.sql(GET_STUDENT_DETAIL_QUERY)
            .bind("studentId", studentId)
            .map { row: Row, _ ->
                row.mapToStudentDto()
            }.awaitOneOrNull()
    }

    /*
    * Override function
    * */
    @Transactional
    override suspend fun save(entity: Students): Students{
        client.sql(SAVE_STUDENT_QUERY)
            .bindValues(paramMap(entity))
            .await()
        return entity
    }

    @Transactional
    override suspend fun update(entity: Students): Students {
        client.sql(UPDATE_STUDENT_QUERY)
            .bindValues(paramMap(entity))
            .fetch()
            .awaitRowsUpdated()
        return entity
    }

    override suspend fun getById(id: Long): Students? {
        return client.sql(GET_STUDENT_QUERY)
            .bind("studentId", id)
            .map { row: Row, _ ->
                row.mapToStudent()
            }.awaitOneOrNull()
    }

    override fun getAll(): Flow<Students>  {
        return client.sql(GET_STUDENTS_QUERY)
            .map { row: Row, _ ->
                row.mapToStudent()
            }
            .all()
            .asFlow()
    }

    @Transactional
    override suspend fun delete(id: Long): Boolean {
        val rowEffect = client.sql(DELETE_STUDENT_QUERY)
            .bind("studentId", id)
            .fetch()
            .awaitRowsUpdated()
        return rowEffect > 0
    }


    private fun paramMap(students: Students): Map<String, Any> = mapOf(
        "studentId" to students.studentId!!,
        "studentName" to students.studentName,
        "gender" to  students.gender,
        "dateOfBirth" to students.dateOfBirth,
        "degreeLevelId" to students.degreeLevelId,
        "majorId" to students.majorId,
        "generation" to students.generation
    )

    private fun Row.mapToStudent(): Students = Students(
        studentId = this.get("student_id", Long::class.java),
        studentName = this.get("student_name", String::class.java)!!,
        gender = this.get("gender", String::class.java)!!,
        dateOfBirth = this.get("date_of_birth", LocalDate::class.java)!!,
        degreeLevelId = this.get("degree_level_id", String::class.java)!!,
        majorId = this.get("major_id", String::class.java)!!,
        generation = this.get("generation", Int::class.java)!!
    )

    private fun Row.mapToStudentDto(): StudentDto = StudentDto(
        studentId = this.get("studentId", Long::class.java),
        studentName = this.get("StudentName", String::class.java)!!,
        gender = this.get("gender", String::class.java)!!,
        dateOfBirth = this.get("dateOfBirth", LocalDate::class.java)!!,
        degreeLevel = this.get("degreeLevel", String::class.java)!!,
        majorName = this.get("majorName", String::class.java)!!,
        generation = this.get("generation", Int::class.java)!!
    )
}
