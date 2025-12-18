/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.repository

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.*
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.CompareValue
import sru.edu.sru_lib_management.core.domain.dto.MajorPurpose
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail
import sru.edu.sru_lib_management.core.domain.dto.attend.StaffAttendDto
import sru.edu.sru_lib_management.core.domain.dto.dashbord.DayVisitor
import sru.edu.sru_lib_management.core.domain.dto.dashbord.TotalMajorVisitor
import sru.edu.sru_lib_management.core.domain.model.Attend
import sru.edu.sru_lib_management.core.domain.model.VisitorType
import sru.edu.sru_lib_management.core.domain.repository.AttendRepository
import sru.edu.sru_lib_management.utils.checkEntryId
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

@Component
class AttendRepositoryImp(
    private val client: DatabaseClient
) : AttendRepository {

    override fun getAllAttendByDate(date: LocalDate): Flow<Attend> {
        return client.sql("SELECT * FROM attend where attend_date = :date")
            .bind("date", date)
            .map { row: Row, _ -> row.mapToAttend() }
            .all()
            .asFlow()
    }

    override fun getCustomAttend(date: LocalDate): Flow<List<Attend>> = client
        .sql("Call GetAttendByCustomTime(:date)")
        .bind("date", date)
        .map { row: Row, _ ->
            row.mapToAttend()
        }
        .all()
        .collectList()
        .asFlow()

    override fun getAttendDetail(date: LocalDate): Flow<AttendDetail> {
        return client.sql("CALL GetAttendDetail(:date)")
            .bind("date", date)
            .map { row: Row, _ -> row.mapToAttendDetail() }
            .all()
            .asFlow()
    }

    override suspend fun save(entity: Attend): Attend {
        var attendId = 0L
        val result = client.sql(SAVE_ATTEND_QUERY)
            .filter { s: Statement, next: ExecuteFunction ->
                next.execute(s.returnGeneratedValues("attend_id"))
            }.checkIdForSave(paramMap(entity)).fetch().awaitOneOrNull()
        if (result != null && result.contains("attend_id")){
            val attId = result["attend_id"] as Long
            attendId = attId
        }
        val attends = Attend(
            attendId,
            entity.visitorId,
            entity.entryTimes,
            entity.exitTimes,
            entity.purpose,
            entity.attendDate
        )
        return attends
    }

    override suspend fun update(entity: Attend): Attend {
        client.sql(UPDATE_ATTEND_QUERY)
            .bindValues(paramMap(entity))
            .fetch()
            .awaitRowsUpdated()
        return entity
    }

    override suspend fun getById(id: Long): Attend? {
        return client.sql(GET_ATTEND_QUERY)
            .bind("attendId", id)
            .map { row: Row, _ ->
                row.mapToAttend()
            }.awaitSingleOrNull()
    }

    override fun getAll(): Flow<Attend> {
        return client.sql(GET_ALL_ATTEND_QUERY)
            .map { row: Row, _ ->
                row.mapToAttend()
            }.flow()
    }

    override suspend fun delete(id: Long): Boolean {
        val rowEffect = client.sql(DELETE_ATTEND_QUERY)
            .bind("attendId", id)
            .fetch()
            .awaitRowsUpdated()
        return rowEffect > 0
    }

    override suspend fun updateExitingTime(attendId: Long, exitingTimes: LocalTime, studentId: Long, date: LocalDate): Long {
        val rowEffect = client.sql(UPDATE_EXIT_TIME)
            .bind("exitingTimes", exitingTimes)
            .bind("attendId", attendId)
            .bind("date", date)
            .fetch()
            .awaitRowsUpdated()
        return if (rowEffect > 0)
            studentId
        else 0
    }

    override suspend fun updateStaffExitingTime(
        attendId: Long,
        exitingTimes: LocalTime,
        staffId: String,
        date: LocalDate,
    ): String {
        val rowEffected = client.sql(UPDATE_STAFF_EXITING_TIME)
            .bind("attendId", attendId)
            .bind("exitingTimes", exitingTimes)
            .bind("date", date)
            .fetch()
            .awaitRowsUpdated()
        return if (rowEffected > 0)
            staffId
        else ""
    }

    override suspend fun count(date: LocalDate, period: Int): Int? {
        return if (period != 0){
            client.sql("CALL CountAttendByPeriod(:date, :period)")
                .bind("date", date)
                .bind("period", period)
                .map {row ->
                    row.get("current_value", Int::class.java)
                }
                .awaitSingle()
        }else{
            client.sql("CALL CountTotalAttend()")
                .map {row ->
                    row.get("attendance_count", Int::class.java)
                }
                .awaitSingle()
        }
    }

    override suspend fun getAttendByEntryId(entryId: String, date: LocalDate): List<Attend?> {
        return if (entryId.checkEntryId() is Long)
            client.sql(GET_ATTEND_QUERY_BY_STUDENT_ID)
                .bind("studentId", entryId)
                .bind("date", date)
                .map { row: Row, _ ->
                    row.mapToAttend()
                }
                .flow()
                .toList()
        else
            client.sql(GET_ATTEND_QUERY_BY_STAFF_ID)
                .bind("staffId", entryId)
                .bind("date", date)
                .map { row: Row, _ ->
                    row.mapToAttend()
                }
                .flow()
                .toList()
    }

    /// Count the total major and total of each major
    override suspend fun getWeeklyVisit(): List<DayVisitor> {
        val today = LocalDate.now()
        val thisWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastWeekMonday = thisWeekMonday.minusWeeks(1)
        val lastWeekSunday = lastWeekMonday.plusDays(6)
        val params = mapOf(
            "monday" to lastWeekMonday,
            "sunday" to lastWeekSunday
        )
        return client.sql("CALL CountAttendPerWeek(:monday, :sunday)")
            .bindValues(params)
            .map { row, _ ->
                val dayName = row.get("day_name", String::class.java)!!
                val count = row.get("count", Int::class.java)!!
                DayVisitor(day = dayName, count = count)
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    /// Count the total major and total of each major
    override suspend fun countCurrentAndPreviousAttend(date: LocalDate, period: Int): CompareValue {
        val param = mapOf(
            "date" to date,
            "period" to period
        )
        return client.sql("CALL CountAttendByPeriod(:date, :period)")
            .bindValues(param)
            .map {row ->
                CompareValue(
                    row.get("current_value", Int::class.java)!!,
                    row.get("previous_value", Int::class.java)!!
                )
            }
            .one()
            .awaitSingle()
    }

    /// Count the total major and total of each major
    override suspend fun totalMajorVisit(): List<TotalMajorVisitor> {
        return client.sql("CALL CountMajorAttendLib()")
            .map { row ->
                val major = row.get("Major", String::class.java)!!
                val amount = row.get("Amount", Int::class.java)!!
                TotalMajorVisitor(
                    majorName = major,
                    totalAmount = amount
                )
            }
            .all()
            .collectList()
            .awaitSingle()
            .toList()
    }

    override fun getAllAttendDetail(visitorType: VisitorType): Flow<StudentAttendDetail> {
        return client.sql("CALL GetAllAttendDetail(:p_visitor_type)")
            .bind("p_visitor_type", visitorType.name)
            .map { row: Row, _ ->
                row.mapToStudentAttendDetail()
            }
            .all()
            .asFlow()
    }

    override fun getAttendDetailByPeriod(
        date: LocalDate,
        entryTime: LocalTime,
        exitingTime: LocalTime,
    ): Flow<StudentAttendDetail> {
        return client.sql("CALL GetAttendDetailByPeriod(:date, :entryTime, :exitingTime, :p_visitor_type)")
            .bind("date", date)
            .bind("entryTime", entryTime)
            .bind("exitingTime", exitingTime)
            .bind("p_visitor_type", VisitorType.STUDENT.name)
            .map { row: Row, _ ->
                row.mapToStudentAttendDetail()
            }
            .flow()
    }

    override suspend fun getAttendDetailById(attendId: Long): StudentAttendDetail? {
        return client.sql("CALL GetAttendDetailById(:Id, :p_visitor_type)")
            .bind("p_visitor_type", VisitorType.STUDENT.name)
            .bind("Id", attendId)
            .map { row: Row, _ ->
                row.mapToStudentAttendDetail()
            }
            .awaitOneOrNull()
    }

    override suspend fun getCustomAttendDetail(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): List<StudentAttendDetail> {
        val map = mapOf( "sDate" to startDate, "eDate" to endDate, "p_visitor_type" to VisitorType.STUDENT.name)
        var statement = client
            .sql("CALL GetCustomAttendDetail(:sDate, :eDate, :p_visitor_type)")
        map.forEach { (k, v) ->
            statement = if (v != null){
                statement.bind(k, v)
            }else{
                val valueType = when(k){
                    "sDate", "eDate" -> LocalDate::class.java
                    else -> String::class.java
                }
                statement.bindNull(k, valueType)
            }
        }
        return statement
            .map { row: Row, _ ->
                row.mapToStudentAttendDetail()
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    override fun getMajorPurpose(): Flow<MajorPurpose> {
        val query = """
            SELECT 
                m.major_name AS major,
                a.purpose AS purpose,
                a.attend_date AS date
            FROM attend a
            JOIN visitors v ON a.visitor_id = v.visitor_id
            JOIN students s ON v.student_id = s.student_id
            JOIN majors m ON s.major_id = m.major_id
        """
        return client.sql(query)
            .map { row ->
                MajorPurpose(
                    majorName = row.get("major", String::class.java)!!,
                    purpose = row.get("purpose", String::class.java)!!,
                    date = row.get("date", LocalDate::class.java)!!
                )
            }
            .flow()
    }

    override fun getAllStaffAttend(): Flow<StaffAttendDto> {
        return client.sql(GET_ALL_STAFF_ATTEND)
            .map { row ->
                StaffAttendDto(
                    attendId = row.get("attend_id", Long::class.java)!!,
                    staffId = row.get("staff_id", String::class.java)!!,
                    staffName = row.get("staff_name", String::class.java)!!,
                    gender = row.get("staff_name", String::class.java)!!,
                    entryTimes = row.get("entry_times", LocalTime::class.java)!!,
                    exitingTimes = row.get("exiting_times", LocalTime::class.java),
                    purpose = row.get("purpose", String::class.java)!!,
                    date = row.get("date", LocalDate::class.java)!!
                )
            }
            .flow()
    }

    ////// Check id for save and update
    private fun DatabaseClient.GenericExecuteSpec.checkIdForSave(
        paramMap: Map<String, Any?>
    ): DatabaseClient.GenericExecuteSpec{
        var boundStatement = this
        paramMap.forEach{ (k, v) ->
            boundStatement = if (v!= null){
                boundStatement.bind(k, v)
            }else{
                val valueType = when(k){
                    "attend_id", "student_id" -> Long::class.java
                    "staffId", "purpose" -> String::class.java
                    "entry_times", "exiting_times" -> LocalTime::class.java
                    "date" -> LocalDate::class.java
                    else -> String::class.java
                }
                boundStatement.bindNull(k, valueType)
            }
        }
        return boundStatement
    }


    private fun paramMap(attend: Attend): Map<String, Any?> = mapOf(
        "visitorId" to attend.visitorId,
        "entryTimes" to attend.entryTimes,
        "exitingTimes" to attend.exitTimes,
        "purpose" to attend.purpose,
        "date" to attend.attendDate
    )

    private fun Row.mapToAttend(): Attend = Attend(
        attendId = this.get("attend_id", Long::class.java)!!,
        visitorId = this.get("visitor_id", Long::class.java)!!,
        entryTimes = this.get("entry_time", LocalTime::class.java)!!,
        exitTimes = this.get("exit_time", LocalTime::class.java),
        purpose = this.get("purpose", String::class.java)!!,
        attendDate = this.get("date", LocalDate::class.java)!!
    )

    private fun Row.mapToStudentAttendDetail(): StudentAttendDetail = StudentAttendDetail(
        this.get("attendId", Long::class.java)!!,
        this.get("student_id", Long::class.java)!!,
        this.get("studentName", String::class.java)!!,
        this.get("gender", String::class.java)!!,
        this.get("majorName", String::class.java)!!,
        this.get("degree_level", String::class.java)!!,
        this.get("generation", Int::class.java)!!,
        this.get("entryTimes", LocalTime::class.java)!!,
        this.get("exitingTime", LocalTime::class.java),
        this.get("purpose", String::class.java)!!,
        this.get("date", LocalDate::class.java)!!,
        status = null
    )

    private fun Row.mapToAttendDetail(): AttendDetail = AttendDetail(
        this.get("id", String::class.java)!!,
        this.get("name", String::class.java)!!,
        this.get("entry_time", LocalTime::class.java)!!,
        this.get("exiting_time", LocalTime::class.java),
        this.get("purpose", String::class.java)!!,
        status = null
    )


    companion object {
        const val SAVE_ATTEND_QUERY = """
            INSERT INTO attend(visitor_id, entry_time, exit_time, attend_date, purpose)
            VALUES(:visitorId, :entryTimes, :exitTimes, :attendDate, :purpose);
        """
        const val UPDATE_ATTEND_QUERY = """
            UPDATE attend set visitor_id = :visitorId, entry_time = :entryTimes,
            exit_time = :exitTimes, attend_date = :attendDate, purpose = :purpose
            WHERE attend_id = :attendId;
        """
        const val DELETE_ATTEND_QUERY = "DELETE FROM attend WHERE attend_id = :attendId;"
        const val GET_ATTEND_QUERY = "SELECT * FROM attend WHERE attend_id = :attendId;"

        const val GET_ATTEND_QUERY_BY_STUDENT_ID = """
            SELECT * FROM vw_attend_details
                WHERE visitor_type = 'STUDENT'
                  AND student_id = :studentId
                  AND attend_date = :date
        """
        const val GET_ATTEND_QUERY_BY_STAFF_ID = """
            SELECT * FROM vw_attend_details
            WHERE visitor_type = 'SRU_STAFF'
              AND sru_staff_id = :staffId
              AND attend_date = :date
        """
        const val GET_ALL_ATTEND_QUERY = "SELECT * FROM attend;"

        const val UPDATE_EXIT_TIME = "UPDATE attend SET exit_time = :exitingTimes WHERE attend_id = :attendId AND attend_date = :date;"
        const val UPDATE_STAFF_EXITING_TIME = """
            UPDATE attend set exit_time = :exitingTimes WHERE attend_id = :attendId AND attend_date = :date;
        """

        const val GET_ALL_STAFF_ATTEND = """
            SELECT
                attend_id,
                sru_staff_id   AS staff_id,
                sru_staff_name AS staff_name,
                gender,
                entry_time     AS entryTimes,
                exit_time      AS exitingTimes,
                purpose,
                attend_date    AS date
            FROM vw_attend_details
            WHERE visitor_type = 'SRU_STAFF'
        """
    }

}