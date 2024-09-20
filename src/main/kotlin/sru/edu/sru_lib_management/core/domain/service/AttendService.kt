/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.*
import sru.edu.sru_lib_management.core.domain.dto.analytic.MajorAttendBorrowed
import sru.edu.sru_lib_management.core.domain.dto.analytic.TotalStudentAttendByTime
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDto
import sru.edu.sru_lib_management.core.domain.dto.attend.StaffAttendDto
import sru.edu.sru_lib_management.core.domain.dto.dashbord.TotalMajorVisitor
import sru.edu.sru_lib_management.core.domain.dto.dashbord.WeeklyVisitor
import sru.edu.sru_lib_management.core.domain.model.Attend
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Service
interface AttendService {
    fun getAllAttend(): CoreResult<Flow<Attend>>
    fun getAllAttendByDate(date: LocalDate): Flow<Attend>
    fun getAttendDetails(date: LocalDate): Flow<AttendDetail>

    fun getAllStudentAttendDetail(date: LocalDate): Flow<StudentAttendDetail>
    suspend fun saveAttend(attendDto: AttendDto): CoreResult<Attend>
    suspend fun updateAttend(attend: Attend): CoreResult<Attend>
    suspend fun deleteAttend(attendId: Long): CoreResult<Boolean>
    suspend fun getCustomAttByDate(date: LocalDate): Flow<List<Attend>>
    suspend fun getAttend(attendId: Long): CoreResult<Attend?>
    suspend fun getAttByEntryId(entryId: String, date: LocalDate): CoreResult<List<Attend?>>
    suspend fun updateExitingTime(entryId: String, exitingTime: LocalTime): CoreResult<String>
    suspend fun countAttendCustomTime(date: LocalDate, period: Int): CoreResult<Int?>

    suspend fun getWeeklyVisit(): CoreResult<WeeklyVisitor>
    suspend fun analyticAttend(date: LocalDate, period: Int): CoreResult<Analyze>

    suspend fun getTotalMajorVisit(): CoreResult<List<TotalMajorVisitor>>

    fun getAttendDetailByPeriod(
        date: LocalDate,
        entryTime: LocalTime,
        exitingTime: LocalTime,
        onEachAttend: (StudentAttendDetail) -> Unit
    ): Flow<StudentAttendDetail>

    // get attend detail by id
    suspend fun getAttendDetailById(attendId: Long): StudentAttendDetail?

    fun countDuration(startDate: LocalDate?, endDate: LocalDate?): Flow<DurationSpent>

    // Purpose
    suspend fun getPurposes(major: String?, startDate: LocalDate, endDate: LocalDate): List<PurposeDto>
    suspend fun getMostAttend(startDate: LocalDate, endDate: LocalDate): List<MajorAttendBorrowed>
    suspend fun countAttendByOpenTime(startDate: LocalDate, endDate: LocalDate): TotalStudentAttendByTime

    suspend fun getPurposeByMonth(major: String?, startMonth: YearMonth?, endMonth: YearMonth?): List<PurposeByMonthDto>
    suspend fun getAllStaffAttend(): List<StaffAttendDto>
}
