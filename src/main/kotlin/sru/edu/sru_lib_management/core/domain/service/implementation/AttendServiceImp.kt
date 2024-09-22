/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import sru.edu.sru_lib_management.common.APIException
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.*
import sru.edu.sru_lib_management.core.domain.dto.analytic.MajorAttendBorrowed
import sru.edu.sru_lib_management.core.domain.dto.analytic.TotalStudentAttendByTime
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail
import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDto
import sru.edu.sru_lib_management.core.domain.dto.attend.StaffAttendDto
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.dashbord.TotalMajorVisitor
import sru.edu.sru_lib_management.core.domain.dto.dashbord.WeeklyVisitor
import sru.edu.sru_lib_management.core.domain.model.Attend
import sru.edu.sru_lib_management.core.domain.repository.AttendRepository
import sru.edu.sru_lib_management.core.domain.repository.SruStaffRepository
import sru.edu.sru_lib_management.core.domain.repository.StudentRepository
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate
import sru.edu.sru_lib_management.utils.OpeningTime.ELEVEN_AM
import sru.edu.sru_lib_management.utils.OpeningTime.FIVE_PM
import sru.edu.sru_lib_management.utils.OpeningTime.FIVE_THIRTY_PM
import sru.edu.sru_lib_management.utils.OpeningTime.SEVEN_AM
import sru.edu.sru_lib_management.utils.OpeningTime.SEVEN_THIRTY_PM
import sru.edu.sru_lib_management.utils.OpeningTime.TWO_PM
import sru.edu.sru_lib_management.utils.checkEntryId
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Component
class AttendServiceImp(
    private val attendRepository: AttendRepository,
    private val studentRepository: StudentRepository,
    private val sruStaffRepository: SruStaffRepository
) : AttendService {

    private val logger = LoggerFactory.getLogger(AttendServiceImp::class.java)
    /*
    * Save new Attend
    * */
    override suspend fun saveAttend(attendDto: AttendDto): CoreResult<Attend> {
        return runCatching {
            var studentId: Long? = null
            var staffId: String? = null
            val entryId = attendDto.entryId
            val checkEntryTime =
                attendDto.entryTimes !in (SEVEN_AM..ELEVEN_AM ) &&
                        attendDto.entryTimes !in TWO_PM ..FIVE_PM &&
                        attendDto.entryTimes !in FIVE_THIRTY_PM..SEVEN_THIRTY_PM
            if (checkEntryTime)
                return CoreResult.ClientError("Closing time.")
            if(entryId.all { it.isDigit() }){
                studentId = entryId.toLong()
            }else{
                staffId = entryId
            }
            if (studentId != null) {
                studentRepository.getById(studentId)
                    ?: return CoreResult.ClientError("Can not find student with this ID: ${attendDto.entryId}")
            }
            else{
                val staff = sruStaffRepository.findById(staffId!!).awaitSingleOrNull()
                staff ?: return CoreResult.ClientError("Can not find staff with this ID: ${attendDto.entryId}")
            }
            val attend = Attend(
                attendId = null,
                studentId = studentId,
                staffId = staffId,
                entryTimes = attendDto.entryTimes,
                exitingTimes = attendDto.exitingTimes,
                purpose = attendDto.purpose,
                date = indoChinaDate()
            )
            attendRepository.save(attend)
        }.fold(
            onSuccess = {att ->
                CoreResult.Success(att)
            },
            onFailure = {e ->
                if (e is Exception) {
                    e.printStackTrace()
                    CoreResult.Failure("${e.message}")
                }
                else
                    CoreResult.ClientError("User input error")
            }
        )
    }

    /*
    * Update attend
    * */
    override suspend fun updateAttend(attend: Attend): CoreResult<Attend> {
        return runCatching {
            attendRepository.getById(attend.attendId!!)
                ?: return CoreResult.ClientError("not found!")
            attendRepository.update(attend)
        }.fold(
            onSuccess = { att ->
                CoreResult.Success(att)
            },
            onFailure = {
                CoreResult.Failure(it.message ?: "Unknown error occurred.")
            }
        )
    }

    /*
    * Delete attend
    * */
    override suspend fun deleteAttend(attendId: Long): CoreResult<Boolean> {
        return runCatching{
            attendRepository.delete(attendId)
        }.fold(
            onSuccess = {
                CoreResult.Success(true)
            },
            onFailure = {
                CoreResult.Failure(it.message ?: "Unknown error occurred.")
            }
        )
    }

    /*
    * Select custom attend by date
    * */
    override suspend fun getCustomAttByDate(date: LocalDate): Flow<List<Attend>> {
        return runCatching {
            attendRepository.getCustomAttend(date)
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                throw APIException(it.message.toString())
            }
        )
    }

    /*
    * Get all attend
    * */
    override fun getAllAttend(): CoreResult<Flow<Attend>> {
        return runCatching {
            attendRepository.getAll()
        }.fold(
            onSuccess = {att ->
                CoreResult.Success(att)
            },
            onFailure = {
                CoreResult.Failure(it.message ?: "Unknown error occurred while get all attend.")
            }
        )
    }

    override fun getAllAttendByDate(date: LocalDate): Flow<Attend> {
        return try {
            attendRepository.getAllAttendByDate(date)
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override fun getAttendDetails(date: LocalDate): Flow<AttendDetail> {
        return try {
            attendRepository.getAttendDetail(date).onEach {
                    if (it.exitingTime == null)
                        it.status = "IN"
                    else it.status = "OUT"
            }
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override fun getAllStudentAttendDetail(date: LocalDate): Flow<StudentAttendDetail> {
        return try {
            attendRepository.getAllAttendDetail()
                .filter { it.date == date }
                .onEach {
                    if (it.exitingTimes == null)
                        it.status = "IN"
                    else
                        it.status = "OUT"
                }
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    /*
    * Select attend by id
    * */
    override suspend fun getAttend(attendId: Long): CoreResult<Attend?> {
        return runCatching{
            attendRepository.getById(attendId)
        }.fold(
            onSuccess = {att ->
                CoreResult.Success(att)
            },
            onFailure = {
                CoreResult.Failure(it.message ?: "An error occurred while get attend.")
            }
        )
    }

    /*
    * Get attend by student id
    * */
    override suspend fun getAttByEntryId(entryId: String, date: LocalDate): CoreResult<List<Attend?>> {
        return runCatching{
            attendRepository.getAttendByEntryId(entryId, date)
        }.fold(
            onSuccess = {
                CoreResult.Success(it)
            },
            onFailure = {
                println(it.printStackTrace())
                CoreResult.Failure("${it.message}")
            }
        )
    }

    /*
    * Update exiting time when student go out
    * */
    override suspend fun updateExitingTime(entryId: String, exitingTime: LocalTime): CoreResult<String> {
        return runCatching {
            val existAttend = attendRepository.getAttendByEntryId(entryId, indoChinaDate())
            var attendId = 0L
            logger.info("$existAttend")
            if (existAttend.isEmpty())
                CoreResult.ClientError("Can not find attend with this student id: $entryId")
            existAttend.forEach {
                if (it!!.exitingTimes == null)
                    attendId = it.attendId!!
            }
            logger.info("$attendId")
            if (entryId.checkEntryId() is Long)
                attendRepository.updateExitingTime(attendId, exitingTime, entryId.toLong(), indoChinaDate())
            else
                attendRepository.updateStaffExitingTime(attendId, exitingTime, entryId, indoChinaDate())

        }.fold(
            onSuccess = {
                CoreResult.Success(it.toString())
            },
            onFailure = {
                println(it.printStackTrace())
                CoreResult.Failure("${it.message}")
            }
        )
    }

    /*
    * Count attend custom by time
    * */
    override suspend fun countAttendCustomTime(date: LocalDate, period: Int): CoreResult<Int?> {
        return if (period < 0){
            CoreResult.ClientError("Opp!")
        }else{
            runCatching {
                attendRepository.count(date, period)
            }.fold(
                onSuccess = {
                    CoreResult.Success(it)
                },
                onFailure = {
                    CoreResult.Failure("${it.message}")
                }
            )
        }
    }
    override suspend fun getWeeklyVisit(): CoreResult<WeeklyVisitor> {
        return runCatching {
            attendRepository.getWeeklyVisit()
        }.fold(
            onSuccess = {
                CoreResult.Success(WeeklyVisitor(it))
            },
            onFailure = {
                println(it.printStackTrace())
                CoreResult.Failure("${it.message}")
            }
        )
    }

    override suspend fun analyticAttend(
        date: LocalDate, period: Int
    ): CoreResult<Analyze> = runCatching {

        val invalidInputData = date > LocalDate.now() || period == 0
        val rightInput = period == 1 || period == 7 || period == 30 || period == 365

        if (invalidInputData || !rightInput)
            return CoreResult.ClientError("Invalid data input.")

        val getCount = attendRepository.countCurrentAndPreviousAttend(date, period)
        val currentValue: Int = getCount.currentValue
        val previousValue = getCount.previousValue

        val percentageChange: Float = if (previousValue == 0){
            if (currentValue == 0) 0f else 100f
        }else{
            ((currentValue - previousValue)).toFloat() / previousValue * 100
        }
        // Return this
        Analyze(currentValue = currentValue, percentage = String.format("%.2f", percentageChange).toFloat())
    }.fold(
        onSuccess = { data ->
            CoreResult.Success(data)
        },
        onFailure = { e ->
            println(e.printStackTrace())
            CoreResult.Failure(e.message.toString())
        }
    )


    override suspend fun getTotalMajorVisit(): CoreResult<List<TotalMajorVisitor>> = runCatching {
        attendRepository.totalMajorVisit()
    }.fold(
        onSuccess = {
            CoreResult.Success(it)
        },
        onFailure = {
            println(it.printStackTrace())
            CoreResult.Failure(it.message.toString())
        }
    )

    override fun getAttendDetailByPeriod(
        date: LocalDate,
        entryTime: LocalTime,
        exitingTime: LocalTime,
        onEachAttend: (StudentAttendDetail) -> Unit
    ): Flow<StudentAttendDetail> {
        return try {
            attendRepository.getAttendDetailByPeriod(date, entryTime, exitingTime)
                .onEach {
                    logger.info("Repository fetched: $it")
                    onEachAttend(it)
                }
                .catch { e ->
                    logger.error("Repository error", e)
                    throw APIException(e.localizedMessage ?: "Unknown error happened.")
                }
        } catch (e: Exception) {
            logger.error("Exception in getAttendDetailByPeriod", e)
            throw APIException(e.localizedMessage ?: "Unknown error happened.")
        }
    }

    override suspend fun getAttendDetailById(attendId: Long): StudentAttendDetail? {
        return try {
            attendRepository.getAttendDetailById(attendId)
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    override fun countDuration(
        startDate: LocalDate?, 
        endDate: LocalDate?
    ): Flow<DurationSpent> {
        try {
            val attendDetail = runBlocking(Dispatchers.IO){
                attendRepository.getCustomAttendDetail(startDate, endDate)
            }
            val filterAttendDetail = attendDetail.filter {
                val exitingTime = it.exitingTimes ?: return@filter false
                val inMorning = exitingTime !in (SEVEN_AM..ELEVEN_AM)
                val inAfternoon = exitingTime !in (TWO_PM..FIVE_PM)
                val inEvening = exitingTime !in (FIVE_THIRTY_PM..SEVEN_THIRTY_PM)
                !(inMorning && inAfternoon && inEvening)
            }
            val durationSpentList = filterAttendDetail
                .groupBy { it.studentId }
                .map { (studentId, attendances) ->
                    val totalTimeSpent = attendances.sumOf { attend ->
                        val entryTime = attend.entryTimes
                        val exitingTime = attend.exitingTimes ?: entryTime
                        Duration.between(entryTime, exitingTime).toMinutes()
                    }
                    val studentName = attendances.first().studentName
                    val major = attendances.first().major
                    val degree = attendances.first().degreeLevel
                    val gen = attendances.first().generation
                    DurationSpent(studentId, studentName, major, degree, gen, totalTimeSpent.toFloat())
                }
            return durationSpentList.sortedByDescending { it.totalTimeSpent }.asFlow()
        }catch (e: Exception){
            e.printStackTrace()
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "${e.message}")
        }
    }

    override suspend fun getPurposes(
        major: String?,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PurposeDto> {
        return try {
            val data = attendRepository.getMajorPurpose().toList()
                .filter { !it.date.isBefore(startDate) && !it.date.isAfter(endDate) }
            logger.info("GET DATA $data")
            val filterByMajor = if (major != null) {
                val filtered = data.filter { it.majorName == major }
                if (filtered.isEmpty()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid major name: $major")
                filtered
            } else data

            val purposeCountByMajor = mutableMapOf<String, Int>()
            filterByMajor.onEach { m ->
                val purpose = m.purpose.split(", ").map { it.trim() }
                purpose.forEach {
                    purposeCountByMajor[it] = purposeCountByMajor.getOrDefault(it, 0) + 1
                }
            }
            val mapReturn = purposeCountByMajor.map { (purpose, count) ->
                PurposeDto( purposeType = purpose,
                    amountData = count
                )
            }
            mapReturn
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun getMostAttend(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MajorAttendBorrowed> {
        return try {
            val allMajor = attendRepository.getCustomAttendDetail(startDate, endDate).toList()
            val totalMajor = allMajor.size.toFloat()
            val majorCount = allMajor.groupingBy { it.major }.eachCount()
            majorCount.map { (major, count) ->
                val percentage = (count.toFloat() / totalMajor) * 100
                MajorAttendBorrowed(
                    major,
                    count,
                    String.format("%.2f", percentage).toFloat()
                )
            }.sortedByDescending { it.times }
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun countAttendByOpenTime(
        startDate: LocalDate,
        endDate: LocalDate
    ): TotalStudentAttendByTime {
        return try {
            val allAttend = attendRepository.getCustomAttendDetail(startDate, endDate).toList()
            val totalAttend: Int = allAttend.size
            val totalFemale: Int = allAttend.count { it.gender === "Female" }
            val totalMorningAttend: Int = allAttend.count { it.entryTimes in SEVEN_AM..ELEVEN_AM }
            val totalAfternoonAttend: Int = allAttend.count { it.entryTimes in TWO_PM..FIVE_PM}
            val totalEveningAttend: Int = allAttend.count { it.entryTimes in FIVE_THIRTY_PM..SEVEN_THIRTY_PM}
            TotalStudentAttendByTime(
                totalAttend,
                totalFemale,
                totalMorningAttend,
                totalAfternoonAttend,
                totalEveningAttend
            )
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun getPurposeByMonth(
        major: String?, startMonth: YearMonth?, endMonth: YearMonth?
    ): List<PurposeByMonthDto>{
        val defaultStartMonth = YearMonth.of(YearMonth.now().year, 1)
        val defaultEndMonth = YearMonth.now()

        val processStartMonth = startMonth ?: defaultStartMonth
        val processEndMonth = endMonth ?: defaultEndMonth

        return try {
            val data = attendRepository.getMajorPurpose().toList()
                .filter {
                    val yearMonth = YearMonth.from(it.date)
                    !yearMonth.isBefore(processStartMonth) && !yearMonth.isAfter(processEndMonth)
                }
            val filterByMajor = if (major != null) {
                val filtered = data.filter { it.majorName == major }
                if (filtered.isEmpty()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid major name: $major")
                filtered
            } else data

            filterByMajor.groupBy { YearMonth.from(it.date) }
                .map { (month, purposes) ->
                    // Step 4: Count occurrences of each purpose for the month
                    val purposeCount = mutableMapOf<String, Int>()
                    purposes.forEach { m ->
                        m.purpose.split(", ").forEach { purpose ->
                            val trimmedPurpose = purpose.trim()
                            purposeCount[trimmedPurpose] = purposeCount.getOrDefault(trimmedPurpose, 0) + 1
                        }
                    }

                    // Step 5: Create PurposeByMonthDto with counts for each purpose type
                    PurposeByMonthDto(
                        other = purposeCount.getOrDefault("Other", 0),
                        reading = purposeCount.getOrDefault("Reading", 0),
                        assignment = purposeCount.getOrDefault("Assignment", 0),
                        usePc = purposeCount.getOrDefault("Use PC", 0),
                        month = month
                    )
                }
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun getAllStaffAttend(): List<StaffAttendDto> {
        return try {
            attendRepository.getAllStaffAttend().toList()
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }


    ////////


}