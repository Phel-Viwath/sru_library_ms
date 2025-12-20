/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import okio.IOException
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.StudentDto
import sru.edu.sru_lib_management.core.domain.model.Students
import sru.edu.sru_lib_management.core.domain.service.StudentService
import sru.edu.sru_lib_management.utils.ResponseStatus.ACCEPTED
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.CREATED
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR
import sru.edu.sru_lib_management.utils.ResponseStatus.OK


@Component
class StudentHandler(
    private val studentService: StudentService
) {

    private val logger = LoggerFactory.getLogger(StudentHandler::class.java)

    /*
     * Add student to Database
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun saveStudent(request: ServerRequest): ServerResponse {

        val students = request.bodyToMono(Students::class.java).awaitFirstOrNull()
        val areFieldBlank = students?.studentId == null || students.studentName.isBlank() || students.gender.isBlank()

        return if (!areFieldBlank){
            when(val result = studentService.saveStudent(students!!)){
                is CoreResult.Success ->
                    ServerResponse.status(CREATED).bodyValueAndAwait(result.data!!)
                is CoreResult.Failure ->
                    ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
                is CoreResult.ClientError ->
                    ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
            }
        }
        else
            ServerResponse.badRequest().buildAndAwait()
    }

    // Get all student using flux as Flow
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getAllStudents(): ServerResponse {
       return try {
           logger.info("Get ALL Students")
           val list: Flow<StudentDto> = studentService.getAllStudentDetail()
           ServerResponse.ok().bodyAndAwait(list)
       }catch (e: Exception){
           throw IOException(e.message)
       }
    }

    /*
     * get student from Database by id
     */

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getStudentById(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val studentId = request.pathVariable("studentId").toLong()
        // get student
        when(val result = studentService.getStudent(studentId)){
            is CoreResult.Success ->
                ServerResponse.status(OK).bodyValueAndAwait(result.data!!)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /*
     * Update student from Database
     */

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    suspend fun updateStudent(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val students = request.bodyToMono(Students::class.java).awaitFirstOrNull()
        val studentId = request.pathVariable("studentId").toLong()
        if (students == null){
            return@coroutineScope ServerResponse.status(BAD_REQUEST).buildAndAwait()
        }
        when(val result = studentService.updateStudent(studentId, students)) {
            is CoreResult.Success ->
                ServerResponse.status(ACCEPTED).bodyValueAndAwait(result.data!!)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /*
    * Delete student from Database
     */

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    suspend fun deleteStudent(request: ServerRequest): ServerResponse {
        val studentId = request.pathVariable("studentId").toLong()
        return when(val result = studentService.deleteStudent(studentId)){
            is CoreResult.Success ->
                ServerResponse.status(OK).bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)

        }
    }
}
