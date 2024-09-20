/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.CompareValue
import sru.edu.sru_lib_management.core.domain.dto.MajorPurpose
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail
import sru.edu.sru_lib_management.core.domain.dto.attend.StaffAttendDto
import sru.edu.sru_lib_management.core.domain.dto.dashbord.DayVisitor
import sru.edu.sru_lib_management.core.domain.dto.dashbord.TotalMajorVisitor
import sru.edu.sru_lib_management.core.domain.model.Attend
import sru.edu.sru_lib_management.core.domain.repository.crud.ICrudRepository
import java.time.LocalDate
import java.time.LocalTime

@Repository
interface AttendRepository : ICrudRepository<Attend, Long> {
    fun getAllAttendByDate(date: LocalDate): Flow<Attend>
    fun getCustomAttend(date: LocalDate): Flow<List<Attend>>
    fun getAttendDetail(date: LocalDate): Flow<AttendDetail>
    suspend fun updateExitingTime(attendId: Long, exitingTimes: LocalTime, studentId: Long, date: LocalDate): Long
    suspend fun updateStaffExitingTime(attendId: Long, exitingTimes: LocalTime, staffId: String, date: LocalDate): String
    suspend fun count(date: LocalDate, period: Int): Int?
    suspend fun getAttendByEntryId(entryId: String, date: LocalDate): List<Attend?>
    suspend fun getWeeklyVisit(): List<DayVisitor>

    suspend fun countCurrentAndPreviousAttend(date: LocalDate, period: Int): CompareValue
    suspend fun totalMajorVisit(): List<TotalMajorVisitor>

    fun getAllAttendDetail(): Flow<StudentAttendDetail>
    fun getAttendDetailByPeriod(date: LocalDate, entryTime: LocalTime, exitingTime: LocalTime): Flow<StudentAttendDetail>
    // get attend detail
    suspend fun getAttendDetailById(attendId: Long): StudentAttendDetail?

    suspend fun getCustomAttendDetail(startDate: LocalDate?, endDate: LocalDate?): List<StudentAttendDetail>

    fun getMajorPurpose(): Flow<MajorPurpose>

    fun getAllStaffAttend(): Flow<StaffAttendDto>

}
