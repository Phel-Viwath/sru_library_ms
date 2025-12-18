package sru.edu.sru_lib_management.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.r2dbc.core.bind
import org.springframework.r2dbc.core.flow
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import sru.edu.sru_lib_management.core.domain.dto.DurationSpent
import sru.edu.sru_lib_management.core.domain.dto.PurposeByMonthDto
import sru.edu.sru_lib_management.core.domain.dto.PurposeDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.*
import sru.edu.sru_lib_management.core.domain.repository.AnalyticRepository
import java.time.LocalDate
import java.time.YearMonth

@Component
class AnalyticRepositoryImp(
    private val client: DatabaseClient
) : AnalyticRepository{

    //private val logger = LoggerFactory.getLogger(AnalyticRepositoryImp::class.java)

    override fun getBookEachCollege(
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<BookEachCollege> {
        return client.sql("CALL GetBookForEachCollege(:startMonth, :endMonth)")
            .bindNullable("startMonth", startMonth?.toString(), String::class.java)
            .bindNullable("endMonth", endMonth?.toString(), String::class.java)
            .map { row ->
                Triple(
                    row.get("collegeName", String::class.java)!!,
                    row.get("language", String::class.java)!!,
                    row.get("bookCount", Int::class.java)!!
                )
            }
            .all()
            .collectList()
            .flatMapMany { rows ->
                val groupedData = rows.groupBy { it.first }
                    .map { (collegeName, collegeRows) ->
                        BookEachCollege(
                            collegeName = collegeName,
                            bookEachLanguage = collegeRows.associate { it.second to it.third }
                        )
                    }
                Flux.fromIterable(groupedData)
            }
            .asFlow()

    }

    override fun getBookIncome(
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<BookIncome> {
        return client.sql("CALL GetBookIncome(:startMonth, :endMonth)")
            .bindNullable("startMonth", startMonth?.toString(), String::class.java)
            .bindNullable("endMonth", endMonth?.toString(), String::class.java)
            .map { row ->
                BookIncome(
                    month = row.get("month", String::class.java) ?: "",
                    donation = row.get("donation", Int::class.java) ?: 0,
                    universityFunding = row.get("university_funding", Int::class.java) ?: 0,
                )
            }
            .flow()
    }

    override suspend fun getTotalBookEachLanguage(): TotalBook {
        return client.sql("CALL GetBookLanguage()")
            .map { row ->
                Pair(
                    row.get("languageName", String::class.java)!!,
                    row.get("totalBookPerLanguage", Int::class.java)!!
                )
            }
            .all()
            .collectList()
            .map { languageBookCounts ->
                // Calculate the totalBook and create the bookEachLanguage map
                val totalBook = languageBookCounts.sumOf { it.second }
                val bookEachLanguage = languageBookCounts.associate { it.first to it.second }
                TotalBook(totalBook = totalBook, bookEachLanguage = bookEachLanguage)
            }
            .awaitSingle()
    }

    override fun getPurposeCount(
        major: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<PurposeDto> {
        return client.sql("CALL PurposeCount(:startDate, :endDate, :major)")
            .bindNullable("startDate", startDate, LocalDate::class.java)
            .bindNullable("endDate", endDate, LocalDate::class.java)
            .bindNullable("major", major, String::class.java)
            .map {  row ->
                PurposeDto(
                    purposeType = row.get("purpose_category", String::class.java) ?: "Unknown",
                    amountData = row.get("total_count", Int::class.java) ?: 0
                )
            }
            .flow()
    }

    override fun getTimeSpend(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<DurationSpent> {
        return client.sql("CALL CountDuration(:startDate, :endDate)")
            .bindNullable("startDate", startDate, LocalDate::class.java)
            .bindNullable("endDate", endDate, LocalDate::class.java)
            .map { row ->
                DurationSpent(
                    studentId = row.get("studentId", Long::class.java)!!,
                    studentName = row.get("studentName", String::class.java)!!,
                    major = row.get("major", String::class.java)!!,
                    degree = row.get("degree", String::class.java)!!,
                    generation = row.get("generation", Int::class.java)!!,
                    totalTimeSpent = row.get("totalTimeSpent", Float::class.java)!!
                )
            }
            .flow()
    }

    override fun mostMajorAttend(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MajorAttendBorrowed> {
        return client.sql("CALL GetMostAttend(:startDate, :endDate)")
            .bindNullable("startDate", startDate, LocalDate::class.java)
            .bindNullable("endDate", endDate, LocalDate::class.java)
            .map { row ->
                MajorAttendBorrowed(
                    row.get("major", String::class.java)!!,
                    row.get("times", Int::class.java)!!,
                    row.get("percentage", Float::class.java)!!
                )
            }
            .flow()
    }

    override fun getPurposeByMonth(
        major: String?,
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<PurposeByMonthDto> {
        return client.sql("CALL GetPurposeByMonth(:majorName, :startMonth, :endMonth)")
            .bindNullable("majorName", major, String::class.java)
            .bind("startMonth", startMonth?.toString())
            .bind("endMonth", endMonth?.toString())
            .map { row ->
                PurposeByMonthDto(
                    other = row.get("other", Int::class.java)!!,
                    reading = row.get("reading", Int::class.java)!!,
                    assignment = row.get("assignment", Int::class.java)!!,
                    usePc = row.get("usePc", Int::class.java)!!,
                    month = row.get("month", String::class.java)!!.toYearMonth()
                )
            }
            .flow()
    }

    override suspend fun getTotalStudentEntries(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): TotalStudentAttendByTime {
        return client.sql("CALL CountAttendByOpenTime(:startDate, :endDate)")
            .bindNullable("startDate", startDate, LocalDate::class.java)
            .bindNullable("endDate", endDate, LocalDate::class.java)
            .map { row ->
                TotalStudentAttendByTime(
                    totalAttend = row.get("totalAttend", Int::class.java)!!,
                    totalFemale = row.get("totalFemale", Int::class.java)!!,
                    morning = row.get("totalMorningAttend", Int::class.java)!!,
                    afternoon = row.get("totalAfternoonAttend", Int::class.java)!!,
                    evening = row.get("totalEveningAttend", Int::class.java)!!
                )
            }
            .awaitSingle()
    }

    override fun getBorrowDataEachMajor(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MajorAttendBorrowed> {
        return client.sql("CALL GetBorrowDataEachMajor(:startDate, :endDate)")
            .bind("startDate", startDate)
            .bind("endDate", endDate)
            .map { row ->
                MajorAttendBorrowed(
                    majorName = row.get("majorName", String::class.java)!!,
                    times = row.get("times", Int::class.java)!!,
                    percentage = row.get("percentage", Float::class.java)!!
                )
            }
            .flow()
    }

    override fun mostBorrow(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MostBorrow> {
        return client.sql("CALL GetMostBorrowedBooks(:startDate, :endDate)")
            .bindNullable("startDate", startDate, LocalDate::class.java)
            .bindNullable("endDate", endDate, LocalDate::class.java)
            .map { row ->
                MostBorrow(
                    rank = row.get("ranking", Int::class.java)!!,
                    bookTitle = row.get("bookTitle", String::class.java)!!,
                    genre = row.get("genre", String::class.java)!!,
                    borrowQuan = row.get("count", Int::class.java)!!,
                )
            }
            .flow()
    }

    override fun getBorrowAndReturn(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<BorrowReturn> {
        return client.sql("CALL GetBorrowAndReturn(:startDate, :endDate)")
            .bindNullable("startDate", startDate, LocalDate::class.java)
            .bindNullable("endDate", endDate, LocalDate::class.java)
            .map { row ->
                BorrowReturn(
                    month = row.get("month", String::class.java)!!.toYearMonth(),
                    borrow = row.get("borrow", Int::class.java)!!,
                    returned = row.get("returned", Int::class.java)!!
                )
            }
            .flow()
    }


    ////////

    private fun GenericExecuteSpec.bindNullable(name: String, value: Any?, type: Class<*>): GenericExecuteSpec {
        return if (value != null){
            this.bind(name, value)
        }else
            this.bindNull(name, type)
    }
    private fun String.toYearMonth(): YearMonth{
        return YearMonth.parse(this)
    }
}
