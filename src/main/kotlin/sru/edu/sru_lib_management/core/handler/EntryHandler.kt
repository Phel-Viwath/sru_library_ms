/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDto
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.entry.CardEntry
import sru.edu.sru_lib_management.core.domain.dto.entry.Entry
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.core.domain.service.StudentService
import sru.edu.sru_lib_management.infrastructure.websocket.DashboardNotificationService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaTime
import sru.edu.sru_lib_management.utils.OpeningTime.ELEVEN_AM
import sru.edu.sru_lib_management.utils.OpeningTime.FIVE_PM
import sru.edu.sru_lib_management.utils.OpeningTime.FIVE_THIRTY_PM
import sru.edu.sru_lib_management.utils.OpeningTime.SEVEN_AM
import sru.edu.sru_lib_management.utils.OpeningTime.SEVEN_THIRTY_PM
import sru.edu.sru_lib_management.utils.OpeningTime.TWO_PM
import sru.edu.sru_lib_management.utils.ResponseStatus.ACCEPTED
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.CREATED
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR
import sru.edu.sru_lib_management.utils.ResponseStatus.OK
import java.time.LocalTime

@Component
class EntryHandler(
    private val studentService: StudentService,
    private val attendService: AttendService,
    private val dashboardNotificationService: DashboardNotificationService
) {
    private val logger = LoggerFactory.getLogger(EntryHandler::class.java)

    ////
    //  http://localhost:8090/api/v1/entry (path variable)
    //  find student in the database when they scan
    ////
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getStudentById(request: ServerRequest): ServerResponse{
        val id = request.pathVariable("id").toLong()
        return coroutineScope {
            when(val result = studentService.getStudent(id)){
                is CoreResult.Success -> ServerResponse.status(OK).bodyValue(result.data!!).awaitSingle()
                is CoreResult.Failure -> ServerResponse.status(500).bodyValue(result.errorMsg).awaitSingle()
                is CoreResult.ClientError -> ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(result.clientErrMsg).awaitSingle()
            }
        }
    }

    ////
    //  http://localhost:8090/api/v1/entry (request param)
    //  save a new entry when student scans
    ////
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun newEntry(
        request: ServerRequest
    ): ServerResponse {
        val entryId = request.queryParam("entryId").orElse(null)
        val purpose = request.queryParam("purpose").orElse(null)

        if (entryId.isNullOrBlank() || purpose.isNullOrBlank()) {
            return ServerResponse.badRequest().bodyValue("entryId or purpose is missing").awaitSingle()
        }
        return coroutineScope {
            if (purpose.isBlank())
                return@coroutineScope ServerResponse.badRequest().build().awaitSingle()

            val attend = when {
                entryId.toLongOrNull() != null -> AttendDto(
                    studentId = entryId.toLong(),
                    sruStaffId = null,
                    entryTimes = indoChinaTime(),
                    exitingTimes = null,
                    purpose = purpose,
                    attendDate = indoChinaDate()
                )
                else -> AttendDto(
                    studentId = null,
                    sruStaffId = entryId,
                    entryTimes = indoChinaTime(),
                    exitingTimes = null,
                    purpose = purpose,
                    attendDate = indoChinaDate()
                )
            }
            when(val result = attendService.saveAttend(attend)){
                is CoreResult.Success -> {
                    dashboardNotificationService.notifyDashboardUpdate()
                    ServerResponse.status(CREATED).bodyValue(result.data).awaitSingle()
                }
                is CoreResult.Failure ->
                    ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValue(result.errorMsg).awaitSingle()
                is CoreResult.ClientError ->
                    ServerResponse.status(BAD_REQUEST).bodyValue(result.clientErrMsg).awaitSingle()
            }
        }
    }

    ////
    //  http://localhost:8090/api/v1/entry (request param)
    //  update exiting time when student scan out
    ////
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun updateExitingTime(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val entryId = request.queryParam("entryId").orElse(null)
            ?: return@coroutineScope ServerResponse.badRequest().buildAndAwait()
        when(val result = attendService.updateExitTimeByVisitorId(entryId, indoChinaTime())){
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValue(result.clientErrMsg).awaitSingle()
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValue(result.errorMsg).awaitSingle()
            is CoreResult.Success -> {
                // Only refresh recent entries (lightweight update)
                dashboardNotificationService.notifyDashboardUpdate()

                ServerResponse.status(ACCEPTED).bodyValue(result.data).awaitSingle()
            }
        }
    }
    ////
    //  http://localhost:8090/api/v1/entry
    //  card data and list of attend in Entry page
    ////
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun recentEntryData(): ServerResponse = coroutineScope {
        var totalExiting = 0
        var entry = 0
        val attendDetail = mutableListOf<StudentAttendDetail>()
        val attendToday: Flow<StudentAttendDetail> = attendService.getAllStudentAttendDetail(indoChinaDate(), null)
        attendToday.collect { attend ->
            if (attend.exitingTimes != null){
                attend.status = "OUT"; totalExiting += 1
            }else{
                entry += 1; attend.status = "IN"
            }
            attendDetail.add(attend)
        }
        val total = attendDetail.size

        if (total == 0){
            val cardEntry = listOf(
                CardEntry(cardType = "Entry", dataNumber = 0),
                CardEntry(cardType = "Exit", dataNumber = 0),
                CardEntry(cardType = "Total", dataNumber = 0)
            )
            return@coroutineScope ServerResponse.ok().bodyValue(
                Entry(cardEntry, attendDetail.reversed())
            ).awaitSingle()
        }

        val cardEntry = listOf(
            CardEntry(cardType = "Entry", dataNumber = entry),
            CardEntry(cardType = "Exit", dataNumber = totalExiting),
            CardEntry(cardType = "Total", dataNumber = total)
        )
        val response = Entry(cardEntry, attendDetail.reversed())

        ServerResponse.ok().bodyValue(response).awaitSingle()
    }

    ////
    //  http://localhost:8090/api/v1/entry/check
    //  card data and list of Attend in Entry page
    ////
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun checkExistingStudent(request: ServerRequest): ServerResponse {
        val entryIdParam = request.queryParam("entryId").orElse(null)

        if (entryIdParam.isNullOrBlank()) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .bodyValue("Missing entryId")
                .awaitSingle()
        }

        val entryId = entryIdParam.toLongOrNull()
            ?: return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .bodyValue("Invalid entryId format. Expected a numeric value.")
                .awaitSingle()

        // Return BAD_REQUEST if entryId is missing or invalid
        var result = "new attend!"
        val morningTime = "07:00:00, 11:00:00"
        val afternoonTime = "14:00:00, 17:00:00"
        val eveningTime = "17:30:00, 19:30:00"
        val testTime = "19:30:00, 23:59:59"

        val (entryTime, exitingTime) = when (indoChinaTime()) {
            in SEVEN_AM .. ELEVEN_AM -> morningTime.split(", ")
            in TWO_PM .. FIVE_PM -> afternoonTime.split(", ")
            in FIVE_THIRTY_PM .. SEVEN_THIRTY_PM -> eveningTime.split(", ")
            else -> testTime.split(", ")
//            else -> return ServerResponse.status(BAD_REQUEST)
//                .bodyValue("Close time.")
//                .awaitSingle()
        }

        logger.info("${indoChinaTime()}")
        attendService.getAttendDetailByPeriod(
            indoChinaDate(),
            LocalTime.parse(entryTime),
            LocalTime.parse(exitingTime)
        ){ attend ->
            if (attend.studentId == entryId)
                result = if (attend.exitingTimes == null) "not exited" else "exited"
        }.toList()

        // Return
        return ServerResponse.status(OK)
            .bodyValue(mapOf("status" to result))
            .awaitSingle()
    }

}