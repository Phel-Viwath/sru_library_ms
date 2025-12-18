/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
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

    fun getAllDonation(startDate: LocalDate?, endDate: LocalDate?): Flow<DonationDetailDto>{
        return try {
            donationService.getDonationDetail()
                .filter { donationDetail ->
                    val isWithinStart = startDate?.let {
                        donationDetail.donateDate.isEqual(it) || donationDetail.donateDate.isAfter(it)
                    } ?: true
                    val isWithinEnd = endDate?.let {
                        donationDetail.donateDate.isEqual(it) || donationDetail.donateDate.isBefore(it)
                    } ?: true

                    // Only return items within the specified range, or all if no range is provided
                    isWithinStart && isWithinEnd
                }
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    suspend fun staffAttendList(startMonth: YearMonth?, endMonth: YearMonth?): List<MonthlyEntry> = try {

        val staffAttendList: Flow<StaffAttendDto> = attendRepository.getAllStaffAttend()
        val listAttend = mutableListOf<MonthlyEntry>()
        val weekendEntries = mutableListOf<StaffAttendDto>()
        val weekdayEntries = mutableListOf<StaffAttendDto>()
        val morningEntries = mutableListOf<StaffAttendDto>()
        val afternoonEntries = mutableListOf<StaffAttendDto>()
        val eveningEntries = mutableListOf<StaffAttendDto>()


        staffAttendList.filter {
            val entryMonth = YearMonth.from(it.date)
            (startMonth == null || !entryMonth.isBefore(startMonth)) && (endMonth == null || !entryMonth.isAfter(endMonth))
        }.collect { attend ->
            // Categorize by weekends vs weekdays
            when (attend.date.dayOfWeek) {
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> weekendEntries.add(attend)
                else -> weekdayEntries.add(attend)
            }
            // Categorize by time slots
            when (attend.entryTimes) {
                in SEVEN_AM..ELEVEN_AM -> morningEntries.add(attend)
                in TWO_PM..FIVE_PM -> afternoonEntries.add(attend)
                in FIVE_THIRTY_PM..SEVEN_THIRTY_PM -> eveningEntries.add(attend)
            }
        }

        morningEntries.groupBy { YearMonth.from(it.date) }
            .forEach { (month, entries) ->
                val totalAttend = entries.size
                val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                listAttend.add(
                    MonthlyEntry(
                        time = "Morning",
                        month = month.toString(), // month as a string
                        entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                    )
                )
            }

        afternoonEntries.groupBy { YearMonth.from(it.date) }
            .forEach { (month, entries) ->
                val totalAttend = entries.size
                val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                listAttend.add(
                    MonthlyEntry(
                        time = "Afternoon",
                        month = month.toString(), // month as a string
                        entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                    )
                )
            }

        morningEntries.groupBy { YearMonth.from(it.date) }
            .forEach { (month, entries) ->
                val totalAttend = entries.size
                val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                listAttend.add(
                    MonthlyEntry(
                        time = "Evening",
                        month = month.toString(), // month as a string
                        entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                    )
                )
            }

        weekendEntries.groupBy { YearMonth.from(it.date) }
            .forEach { (month, entries) ->
                val totalAttend = entries.size
                val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                listAttend.add(
                    MonthlyEntry(
                        time = "Saturday & Sunday",
                        month = month.toString(), // month as a string
                        entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                    )
                )
            }

        listAttend
    }catch (e: Exception){
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
    }


    fun studentAttendList(startMonth: YearMonth?, endMonth: YearMonth?): Flow<MonthlyEntry> = try {
        val allStudentAttendList = runBlocking(Dispatchers.IO) {// Run in coroutines scope Input Output thread (non_blocking)
            attendRepository.getCustomAttendDetail(null, null)
        }.asFlow()
        val listAttend = mutableListOf<MonthlyEntry>()
        val weekendEntries = mutableListOf<StudentAttendDetail>()
        val weekdayEntries = mutableListOf<StudentAttendDetail>()
        val morningEntries = mutableListOf<StudentAttendDetail>()
        val afternoonEntries = mutableListOf<StudentAttendDetail>()
        val eveningEntries = mutableListOf<StudentAttendDetail>()

        runBlocking(Dispatchers.IO) {
            allStudentAttendList.filter {
                val entryMonth = YearMonth.from(it.date)
                (startMonth == null || !entryMonth.isBefore(startMonth)) && (endMonth == null || !entryMonth.isAfter(endMonth))
            }.collect { attend ->
                // Categorize by weekends vs weekdays
                when (attend.date.dayOfWeek) {
                    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> weekendEntries.add(attend)
                    else -> weekdayEntries.add(attend)
                }
                // Categorize by time slots
                when (attend.entryTimes) {
                    in SEVEN_AM..ELEVEN_AM -> morningEntries.add(attend)
                    in TWO_PM..FIVE_PM -> afternoonEntries.add(attend)
                    in FIVE_THIRTY_PM..SEVEN_THIRTY_PM -> eveningEntries.add(attend)
                }
            }

            morningEntries.groupBy { YearMonth.from(it.date) }
                .forEach { (month, entries) ->
                    val totalAttend = entries.size
                    val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                    listAttend.add(
                        MonthlyEntry(
                            time = "Morning",
                            month = month.toString(), // month as a string
                            entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                        )
                    )
                }

            afternoonEntries.groupBy { YearMonth.from(it.date) }
                .forEach { (month, entries) ->
                    val totalAttend = entries.size
                    val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                    listAttend.add(
                        MonthlyEntry(
                            time = "Afternoon",
                            month = month.toString(), // month as a string
                            entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                        )
                    )
                }

            morningEntries.groupBy { YearMonth.from(it.date) }
                .forEach { (month, entries) ->
                    val totalAttend = entries.size
                    val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                    listAttend.add(
                        MonthlyEntry(
                            time = "Evening",
                            month = month.toString(), // month as a string
                            entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                        )
                    )
                }

            weekendEntries.groupBy { YearMonth.from(it.date) }
                .forEach { (month, entries) ->
                    val totalAttend = entries.size
                    val femaleAttend = entries.count { it.gender.equals("Female", ignoreCase = true) }

                    listAttend.add(
                        MonthlyEntry(
                            time = "Saturday & Sunday",
                            month = month.toString(), // month as a string
                            entry = mapOf("TotalAttend" to totalAttend, "FemaleAttend" to femaleAttend)
                        )
                    )
                }
        }

        listAttend.asFlow()
    }catch (e: Exception){

        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
    }


}






