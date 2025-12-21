package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.AttendHandler
import kotlinx.coroutines.flow.Flow

@Configuration
class AttendRoute {

    @Bean
    @FlowPreview
    fun attendRoute(attendHandler: AttendHandler) = coRouter {
        (accept(APPLICATION_JSON) and "api/v1/att").nest {

            /**
             * Gets attendance purposes filtered by major and date range.
             *
             * Query params:
             * - major: String (optional) - Filter by specific major name
             * - startDate: LocalDate (required, format: YYYY-MM-DD) - Start date
             * - endDate: LocalDate (required, format: YYYY-MM-DD) - End date
             *
             * Returns list of [sru.edu.sru_lib_management.core.domain.dto.PurposeDto] with purpose types and counts.
             * Purpose types: Reading, Assignment, Use PC, Other
             *
             * @see AttendHandler.getAttendPurpose
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.getPurposes
             * @see sru.edu.sru_lib_management.core.domain.dto.PurposeDto
             */
            GET("/purpose", attendHandler::getAttendPurpose)

            /**
             * Gets total time spent by students in the library.
             *
             * Calculates duration between entry and exit times for all students.
             * Filters out invalid time ranges (outside opening hours).
             * Returns [Flow] of [sru.edu.sru_lib_management.core.domain.dto.DurationSpent] sorted by total time descending.
             *
             * @see AttendHandler.getDurationSpent
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.countDuration
             * @see sru.edu.sru_lib_management.core.domain.dto.DurationSpent
             */
            GET("/time-spent") { attendHandler.getDurationSpent() }

            /**
             * Gets weekly visitor statistics.
             *
             * Returns attendance count for each day of the previous week (Monday to Sunday).
             * Returns [sru.edu.sru_lib_management.core.domain.dto.dashboard.WeeklyVisitor] containing the list of [sru.edu.sru_lib_management.core.domain.dto.dashboard.DayVisitor].
             *
             * @see AttendHandler.weeklyVisitor
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.getWeeklyVisit
             * @see sru.edu.sru_lib_management.core.domain.dto.dashboard.WeeklyVisitor
             * @see sru.edu.sru_lib_management.core.domain.dto.dashboard.DayVisitor
             */
            GET("/weekly") { attendHandler.weeklyVisitor() }

            /**
             * Gets detailed attendance records for today.
             *
             * Returns [Flow] of [sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail] with visitor info, entry/exit times, and status.
             * Status is automatically set: "IN" if no exit time, "OUT" if exit time exists.
             *
             * @see AttendHandler.getDetails
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.getAttendDetails
             * @see sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail
             */
            GET("/detail") { attendHandler.getDetails() }

            /**
             * Compares attendance count with a previous period.
             *
             * Query params:
             * - date: LocalDate (optional, format: YYYY-MM-DD) - Reference date
             * - period: Int (required) - Period in days (1, 7, 30, or 365)
             *
             * Returns [sru.edu.sru_lib_management.core.domain.dto.Analyze] with current count and percentage change from the previous period.
             * Calculates percentage: ((current - previous) / previous) * 100
             *
             * @see AttendHandler.countAndCompare
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.analyticAttend
             * @see sru.edu.sru_lib_management.core.domain.dto.Analyze
             */
            GET("/compare", attendHandler::countAndCompare)

            /**
             * Counts total attendance for a custom time period.
             *
             * Query params:
             * - date: LocalDate (optional, format: YYYY-MM-DD) - Reference date
             * - period: Int (required) - Period in days
             *
             * Returns count as Int.
             *
             * @see AttendHandler.countCustomAttend
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.countAttendCustomTime
             */
            GET("/count", attendHandler::countCustomAttend)

            /**
             * Gets attendance records filtered by date.
             *
             * Query param:
             * - date: LocalDate (optional, format: YYYY-MM-DD) - Filter date
             *
             * Returns [Flow] of [List] of [sru.edu.sru_lib_management.core.domain.model.Attend] records.
             * Example: Get the last 1 day, 7 days, 1 month, or 1 year.
             *
             * @see AttendHandler.getCustomAttend
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.getCustomAttByDate
             * @see sru.edu.sru_lib_management.core.domain.model.Attend
             */
            GET("/custom", attendHandler::getCustomAttend)

            /**
             * Gets all attendance records.
             *
             * Returns [Flow] of all [sru.edu.sru_lib_management.core.domain.model.Attend] records from the database.
             *
             * @see AttendHandler.getAllAttend
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.getAllAttend
             * @see sru.edu.sru_lib_management.core.domain.model.Attend
             */
            GET("") { attendHandler.getAllAttend() }

            /**
             * Updates exit time for a visitor's current attendance.
             *
             * Query params:
             * - entryId: Long (required) - Visitor ID
             * - exitingTime: LocalTime (required, format: HH:mm:ss) - Exit time
             *
             * Automatically finds today's attendance record with null exit time for the visitor.
             * Updates the exit time for that record.
             *
             * @see AttendHandler.updateExitingTime
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.updateExitingTime
             */
            PUT("", attendHandler::updateExitingTime)

            /**
             * Updates an existing attendance record.
             *
             * Path param: attId - Attendance ID to update
             * Query param: attId (optional) - Can also be passed as query param
             * Requires [sru.edu.sru_lib_management.core.domain.model.Attend] object in request body.
             *
             * Use this endpoint for manual corrections to attendance records.
             * Requires the ADMIN or SUPER_ADMIN role.
             *
             * @see AttendHandler.updateAtt
             * @see sru.edu.sru_lib_management.core.domain.service.AttendService.updateAttend
             * @see sru.edu.sru_lib_management.core.domain.model.Attend
             */
            PUT("/{attId}", attendHandler::updateAtt)
        }
    }
}