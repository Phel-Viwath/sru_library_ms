/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import sru.edu.sru_lib_management.core.domain.dto.DonationDetailDto
import sru.edu.sru_lib_management.core.domain.dto.attend.StaffAttendDto
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.report.MonthlyEntry
import sru.edu.sru_lib_management.core.domain.repository.AttendRepository
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.utils.OpeningTime.ELEVEN_AM
import sru.edu.sru_lib_management.utils.OpeningTime.FIVE_PM
import sru.edu.sru_lib_management.utils.OpeningTime.FIVE_THIRTY_PM
import sru.edu.sru_lib_management.utils.OpeningTime.SEVEN_AM
import sru.edu.sru_lib_management.utils.OpeningTime.SEVEN_THIRTY_PM
import sru.edu.sru_lib_management.utils.OpeningTime.TWO_PM
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Service
class ReportService(
    private val attendRepository: AttendRepository,
    private val bookRepository: BookRepository,
    private val donationService: DonationService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    // Attendance categories data class to hold categorized entries
    private data class AttendanceCategories<T>(
        val morning: MutableList<T> = mutableListOf(),
        val afternoon: MutableList<T> = mutableListOf(),
        val evening: MutableList<T> = mutableListOf(),
        val weekend: MutableList<T> = mutableListOf(),
        val weekday: MutableList<T> = mutableListOf()
    )

    fun getAllDonation(startDate: LocalDate?, endDate: LocalDate?): Flow<DonationDetailDto> {
        return try {
            donationService.getDonationDetail()
                .filter { donationDetail ->
                    val isWithinStart = startDate?.let { start ->
                        donationDetail.donateDate.isEqual(start) || donationDetail.donateDate.isAfter(start)
                    } ?: true
                    val isWithinEnd = endDate?.let { end ->
                        donationDetail.donateDate.isEqual(end) || donationDetail.donateDate.isBefore(end)
                    } ?: true
                    isWithinStart && isWithinEnd
                }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    suspend fun staffAttendList(startMonth: YearMonth?, endMonth: YearMonth?): List<MonthlyEntry> = try {
        val staffAttendList: Flow<StaffAttendDto> = attendRepository.getAllStaffAttend()
        val categories = AttendanceCategories<StaffAttendDto>()

        staffAttendList
            .filter { isWithinDateRange(it.attendDate, startMonth, endMonth) }
            .collect { attend -> categorizeAttendance(attend, categories) }

        buildMonthlyEntries(categories)
    } catch (e: Exception) {
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
    }

    suspend fun studentAttendList(startMonth: YearMonth?, endMonth: YearMonth?): Flow<MonthlyEntry> = flow {
        try {
            val allStudentAttendList = withContext(Dispatchers.IO) {
                attendRepository.getCustomAttendDetail(null, null)
            }.asFlow()

            val categories = AttendanceCategories<StudentAttendDetail>()

            withContext(Dispatchers.IO) {
                allStudentAttendList
                    .filter { isWithinDateRange(it.date, startMonth, endMonth) }
                    .collect { attend -> categorizeAttendance(attend, categories) }
            }

            buildMonthlyEntries(categories).forEach { emit(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * Checks if a date falls within the specified month range
     */
    private fun isWithinDateRange(date: LocalDate, startMonth: YearMonth?, endMonth: YearMonth?): Boolean {
        val entryMonth = YearMonth.from(date)
        return (startMonth == null || !entryMonth.isBefore(startMonth)) &&
                (endMonth == null || !entryMonth.isAfter(endMonth))
    }

    /**
     * Categorizes attendance by weekend/weekday and time slots
     */
    private fun <T> categorizeAttendance(attend: T, categories: AttendanceCategories<T>) {
        val (date, entryTime) = when (attend) {
            is StaffAttendDto -> attend.attendDate to attend.entryTimes
            is StudentAttendDetail -> attend.date to attend.entryTimes
            else -> return
        }

        // Categorize by weekend vs weekday
        when (date.dayOfWeek) {
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> categories.weekend.add(attend)
            else -> categories.weekday.add(attend)
        }

        // Categorize by time slots
        when (entryTime) {
            in SEVEN_AM..ELEVEN_AM -> categories.morning.add(attend)
            in TWO_PM..FIVE_PM -> categories.afternoon.add(attend)
            in FIVE_THIRTY_PM..SEVEN_THIRTY_PM -> categories.evening.add(attend)
        }
    }

    /**
     * Builds monthly entries from categorized attendance data
     */
    private fun <T> buildMonthlyEntries(categories: AttendanceCategories<T>): List<MonthlyEntry> {
        val listAttend = mutableListOf<MonthlyEntry>()

        // Add morning entries
        addMonthlyEntries(categories.morning, "Morning", listAttend)

        // Add afternoon entries
        addMonthlyEntries(categories.afternoon, "Afternoon", listAttend)

        // Add evening entries
        addMonthlyEntries(categories.evening, "Evening", listAttend)

        // Add weekend entries
        addMonthlyEntries(categories.weekend, "Saturday & Sunday", listAttend)

        return listAttend
    }

    /**
     * Helper function to add monthly entries for a specific time period
     */
    private fun <T> addMonthlyEntries(
        entries: List<T>,
        timePeriod: String,
        resultList: MutableList<MonthlyEntry>
    ) {
        entries.groupBy { entry ->
            val date = when (entry) {
                is StaffAttendDto -> entry.attendDate
                is StudentAttendDetail -> entry.date
                else -> return@groupBy null
            }
            YearMonth.from(date)
        }.forEach { (month, monthEntries) ->
            if (month != null) {
                val totalAttend = monthEntries.size
                val femaleAttend = monthEntries.count { entry ->
                    when (entry) {
                        is StaffAttendDto -> entry.gender.equals("Female", ignoreCase = true)
                        is StudentAttendDetail -> entry.gender.equals("Female", ignoreCase = true)
                        else -> false
                    }
                }

                resultList.add(
                    MonthlyEntry(
                        time = timePeriod,
                        month = month.toString(),
                        entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                    )
                )
            }
        }
    }
}







