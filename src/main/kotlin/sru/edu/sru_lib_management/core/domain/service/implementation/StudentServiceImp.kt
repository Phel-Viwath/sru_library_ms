/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.data.repository.StudentRepositoryImp
import sru.edu.sru_lib_management.core.domain.dto.StudentDto
import sru.edu.sru_lib_management.core.domain.model.Students
import sru.edu.sru_lib_management.core.domain.repository.StudentRepository
import sru.edu.sru_lib_management.core.domain.service.StudentService
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR

@Component
class StudentServiceImp(
   private val studentRepository: StudentRepository
) : StudentService {

    private val logger = LoggerFactory.getLogger(StudentService::class.java)

    override suspend fun saveStudent(students: Students): CoreResult<Students?> {
        return runCatching {
            val existing = studentRepository.getById(students.studentId!!)
            if (existing != null)
                return CoreResult.ClientError("Invalid ID")
            studentRepository.save(students)
        }.fold(
            onSuccess = { student ->
                CoreResult.Success(student)
            },
            onFailure = { e ->
                println(e.printStackTrace())
                CoreResult.Failure(e.message ?: "Unknown error occurred.")
            }
        )
    }

    override suspend fun updateStudent(studentId: Long, students: Students): CoreResult<Students?> {
        return runCatching {
            val existingStudent = studentRepository.getById(studentId)
                ?: return@runCatching CoreResult.ClientError("Not found student with ID: $studentId")
            val updateStudents = existingStudent.copy(
                studentName = students.studentName,
                gender = students.gender,
                dateOfBirth = students.dateOfBirth,
                degreeLevelId = students.degreeLevelId,
                majorId = students.majorId,
                generation = students.generation
            )
            studentRepository.update(updateStudents)
        }.fold(
            onSuccess = {
                CoreResult.Success(students)
            },
            onFailure = {e ->
                CoreResult.Failure(e.message ?: "Unknown error occurred.")
            }
        )
    }
    override suspend fun deleteStudent(studentId: Long): CoreResult<Boolean> {
        return runCatching {
            studentRepository.delete(studentId)
        }.fold(
            onSuccess = { CoreResult.Success(true) },
            onFailure = { e ->
                CoreResult.Failure(e.message ?: "Unknown error occurred.")
            }
        )
    }

    override fun getStudents(): Flow<Students> {
        val data =  studentRepository.getAll()
        logger.info("Student Service: $data")
        return data
    }

    override suspend fun getStudent(studentId: Long): CoreResult<StudentDto?> {
        return try{
            val student = studentRepository.getStudentDetailById(studentId)
            if (student != null)
                return CoreResult.Success(student)
            else return CoreResult.ClientError("Incorrect ID")
        }catch (e: Exception){
            CoreResult.Failure(e.message ?: "Unknown error occurred.")
        }
    }

    override fun getAllStudentDetail(): Flow<StudentDto> {
        return studentRepository.getStudentDetail()
            .catch { e ->
                throw ResponseStatusException(INTERNAL_SERVER_ERROR, e.message)
            }
    }


}
