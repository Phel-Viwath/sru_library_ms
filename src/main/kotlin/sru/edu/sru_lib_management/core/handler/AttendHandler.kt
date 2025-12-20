/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail
import sru.edu.sru_lib_management.core.domain.model.Attend
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.CREATED
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR
import java.time.LocalDate
import java.time.LocalTime

@Component
class AttendHandler(
    private val attendService: AttendService,
) {

    //private val logger = LoggerFactory.getLogger(AttendHandler::class.java)

    /*
    * -> http://localhost:8090/api/v1/att
    * Use to update Attend if necessary
    * */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun updateAtt(
        request: ServerRequest
    ): ServerResponse = coroutineScope {

        val attId = request.queryParamOrNull("attId")?.toLong()
        val attend = request.bodyToMono<Attend>().awaitFirst()

        when(val result = attendService.updateAttend(attend.copy(attendId = attId))){
            is CoreResult.Success ->
                ServerResponse.status(CREATED).bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /*
    * ->  http://localhost:8090/api/v1/att
    * Get all attend
    * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getAllAttend(): ServerResponse = coroutineScope {
        when(val result = attendService.getAllAttend()){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /*
    * ->  http://localhost:8090/api/v1/att
    * this end point use to update exiting time
    * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun updateExitingTime(
        request: ServerRequest
    ): ServerResponse = coroutineScope{

        val entryId = request.queryParamOrNull("entryId")?.toLongOrNull()
            ?: return@coroutineScope ServerResponse.badRequest().buildAndAwait()
        val exitingTime = request.queryParam("exitingTime")
            .map { LocalTime.parse(it) }.orElse(null)

        when(val result = attendService.updateExitingTime(entryId, exitingTime)){
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
        }
    }

    /*
    -> http://localhost:8090/api/v1/att/custom get Attend custom by time
    *  Example: Get Last 1 day, 7 days, 1 month or 1 year
    *
    */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getCustomAttend(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val date = request.queryParam("date")
            .map { LocalDate.parse(it) }
            .orElse(null)
        val result: Flow<List<Attend>> = attendService.getCustomAttByDate(date)
        ServerResponse.status(HttpStatus.OK).bodyAndAwait(result)
    }

    /*
    * ->  http://localhost:8090/api/v1/att/count
    * Count number of Student by custom time
    * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun countCustomAttend(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val date = request.queryParam("date")
            .map { LocalDate.parse(it) }
            .orElse(null)
        val period = request.queryParamOrNull("period")?.toInt()
            ?: return@coroutineScope ServerResponse.badRequest().buildAndAwait()

        when(val result = attendService.countAttendCustomTime(date, period)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data!!)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
        }
    }

    /*
   * ->  http://localhost:8090/api/v1/att/compare
   * Count number of Attend by custom time and compare it to the previous time
   * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun countAndCompare(
        request: ServerRequest
    ): ServerResponse = coroutineScope {

        val date = request.queryParam("date")
            .map { LocalDate.parse(it) }
            .orElse(null)
        val period = request.queryParamOrNull("period")?.toInt()
            ?: return@coroutineScope ServerResponse.badRequest().buildAndAwait()


        when(val result = attendService.analyticAttend(date, period)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
        }
    }

    /*
   * ->  http://localhost:8090/api/v1/att/detail
   * Count number of Students by custom time
   * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getDetails(): ServerResponse = coroutineScope {
        val result: Flow<AttendDetail> = attendService.getAttendDetails(indoChinaDate())
        ServerResponse.ok().bodyAndAwait(result)
    }

    /*
  * ->  http://localhost:8090/api/v1/att/weekly
  * Count weekly visit of Student by custom time
  * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun weeklyVisitor(): ServerResponse = coroutineScope {
        when(val result = attendService.getWeeklyVisit()){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getDurationSpent(): ServerResponse = coroutineScope{
        val durationSpentFlow = attendService.countDuration(null, null)
        ServerResponse.ok().bodyAndAwait(durationSpentFlow)
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getAttendPurpose(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val major = request.queryParamOrNull("major")
        val startDate = request.queryParam("startDate")
            .map { LocalDate.parse(it) }
            .orElse(null)
        val endDate = request.queryParam("endDate")
            .map { LocalDate.parse(it) }
            .orElse(null)

        val data = attendService.getPurposes(major, startDate, endDate)
        ServerResponse.ok().bodyValueAndAwait(data)
    }

}