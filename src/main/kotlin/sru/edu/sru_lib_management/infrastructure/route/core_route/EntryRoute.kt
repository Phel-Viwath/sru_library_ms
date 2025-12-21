package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.EntryHandler
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.core.domain.service.StudentService
import sru.edu.sru_lib_management.core.domain.model.Attend
import sru.edu.sru_lib_management.core.domain.dto.entry.Entry
import sru.edu.sru_lib_management.core.domain.dto.entry.CardEntry
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail

@Configuration
class EntryRoute {
    @Configuration
    class EntryRoute {

        @Bean
        @FlowPreview
        fun entryRoute(entryHandler: EntryHandler): RouterFunction<ServerResponse> = coRouter {
            accept(APPLICATION_JSON).nest {
                "/api/v1/entry".nest {

                    /**
                     * Gets recent entry data with statistics for the entry page.
                     *
                     * Returns [Entry] object containing:
                     * - cardEntry: List of [CardEntry] with 3 cards:
                     *   - "Entry": Count of students currently inside (exitingTime is null)
                     *   - "Exit": Count of students who have exited today
                     *   - "Total": Total attendance count for today
                     * - attendDetail: List of [StudentAttendDetail] in reversed order (newest first)
                     *
                     * Process:
                     * 1. Fetches all today's attendance records
                     * 2. Categorizes by status: "IN" (no exit time) or "OUT" (has exit time)
                     * 3. Counts entries, exits, and total
                     * 4. Returns cards with statistics and reversed attendance list
                     *
                     * Returns empty cards (0 values) if no attendance records for today.
                     * Useful for real-time entry/exit monitoring dashboard.
                     *
                     * @see EntryHandler.recentEntryData
                     * @see Entry
                     * @see CardEntry
                     * @see StudentAttendDetail
                     * @see AttendService.getAllStudentAttendDetail
                     */
                    GET("") { entryHandler.recentEntryData() }

                    /**
                     * Checks if a student/staff has already entered during the current time period.
                     *
                     * Query param:
                     * - entryId: Long (required) - Student ID or Staff ID to check
                     *
                     * Determines current library session based on time:
                     * - Morning: 07:00-11:00
                     * - Afternoon: 14:00-17:00
                     * - Evening: 17:30-19:30
                     * - Test period: 19:30-23:59 (after hours, for testing)
                     *
                     * Returns Map with a "status" key containing one of:
                     * - "new attend!" - No attendance record in the current period
                     * - "not exited" - Already entered but hasn't exited yet
                     * - "exited" - Already entered and exited during the current period
                     *
                     * Use cases:
                     * - Prevent duplicate entries in the same session
                     * - Determine if this is the entry or exit scan
                     * - Track student location status (inside/outside)
                     *
                     * Process:
                     * 1. Validates entryId format (must be numeric)
                     * 2. Determines the current time period
                     * 3. Queries attendance for the current period
                     * 4. Checks if the student has active/completed attendance
                     * 5. Returns appropriate status
                     *
                     * @see EntryHandler.checkExistingStudent
                     * @see AttendService.getAttendDetailByPeriod
                     * @see StudentAttendDetail
                     */
                    GET("/check", entryHandler::checkExistingStudent)

                    /**
                     * Gets student information by ID for entry verification.
                     *
                     * Path param:
                     * - id: Long (required) - Student ID from barcode/QR scan
                     *
                     * Returns student information when they scan their ID card:
                     * - Student name
                     * - Student ID
                     * - Major/Faculty
                     * - Photo (if available)
                     * - Other relevant student details
                     *
                     * Used in the entry workflow:
                     * 1. Student scans ID card/QR code
                     * 2. System calls this endpoint to verify and display student info
                     * 3. Staff confirms identity
                     * 4. System proceeds with entry/exit recording
                     *
                     * Returns 404 if the student is not found.
                     *
                     * @see EntryHandler.getStudentById
                     * @see StudentService.getStudent
                     */
                    GET("/{id}", entryHandler::getStudentById)

                    /**
                     * Records a new entry when student/staff scans in.
                     *
                     * Query params (both required):
                     * - entryId: String - Can be Student ID (numeric) or Staff ID (alphanumeric)
                     * - purpose: String - Purpose of a visit (Reading, Assignment, Use PC, Other)
                     *
                     * Process:
                     * 1. Validates entryId and purpose are present
                     * 2. Determines visitor type:
                     *    - Numeric entryId → Student (creates [AttendDto] with studentId)
                     *    - Alphanumeric entryId → Staff (creates [AttendDto] with sruStaffId)
                     * 3. Sets the current time as entryTime
                     * 4. Sets exitingTime as null (not exited yet)
                     * 5. Saves attendance record via [AttendService.saveAttend]
                     * 6. Auto-updates previous exit time if the visitor has unclosed attendance
                     *
                     * Returns saved [Attend] entity with:
                     * - attendId: Generated ID
                     * - visitorId: Associated visitor record
                     * - entryTimes: Current timestamp
                     * - exitTimes: null
                     * - purpose: Selected purpose
                     * - attendDate: Current date
                     *
                     * Example:
                     * ```
                     * POST /api/v1/entry?entryId=12345&purpose=Reading
                     * POST /api/v1/entry?entryId=STAFF001&purpose=Work
                     * ```
                     *
                     * @see EntryHandler.newEntry
                     * @see AttendService.saveAttend
                     * @see sru.edu.sru_lib_management.core.domain.dto.attend.AttendDto
                     * @see Attend
                     */
                    POST("", entryHandler::newEntry)

                    /**
                     * Updates exit time when student/staff scans out.
                     *
                     * Query param:
                     * - entryId: String (required) - Student ID or Staff ID
                     *
                     * Process:
                     * 1. Finds today's attendance record with null exit time
                     * 2. Sets exitTime to the current timestamp
                     * 3. Calculates time spent (duration between entry and exit)
                     * 4. Updates attendance status to "OUT"
                     *
                     * Returns updated attendance information.
                     * Returns 400 if no active (non-exited) attendance is found.
                     *
                     * Note: System uses visitorId internally, which links to either
                     * studentId or sruStaffId depending on a visitor type.
                     *
                     * Example:
                     * ```
                     * PUT /api/v1/entry?entryId=12345
                     * ```
                     *
                     * @see EntryHandler.updateExitingTime
                     * @see AttendService.updateExitTimeByVisitorId
                     */
                    PUT("", entryHandler::updateExitingTime)
                }
            }
        }
    }
}