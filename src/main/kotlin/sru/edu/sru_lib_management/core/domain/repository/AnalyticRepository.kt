package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.core.domain.dto.DurationSpent
import sru.edu.sru_lib_management.core.domain.dto.PurposeByMonthDto
import sru.edu.sru_lib_management.core.domain.dto.PurposeDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookEachCollege
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookIncome
import sru.edu.sru_lib_management.core.domain.dto.analytic.BorrowReturn
import sru.edu.sru_lib_management.core.domain.dto.analytic.MajorAttendBorrowed
import sru.edu.sru_lib_management.core.domain.dto.analytic.MostBorrow
import sru.edu.sru_lib_management.core.domain.dto.analytic.TotalBook
import sru.edu.sru_lib_management.core.domain.dto.analytic.TotalStudentAttendByTime
import java.time.LocalDate
import java.time.YearMonth

@Repository
interface AnalyticRepository {
    // book
    fun getBookEachCollege(startMonth: YearMonth?, endMonth: YearMonth?): Flow<BookEachCollege>
    fun getBookIncome(startMonth: YearMonth?, endMonth: YearMonth?): Flow<BookIncome>
    suspend fun getTotalBookEachLanguage(): TotalBook

    // attend
    fun getPurposeCount(major: String?, startDate: LocalDate?, endDate: LocalDate?): Flow<PurposeDto>
    fun getTimeSpend(startDate: LocalDate?, endDate: LocalDate?): Flow<DurationSpent>
    fun mostMajorAttend(startDate: LocalDate?, endDate: LocalDate?): Flow<MajorAttendBorrowed>
    fun getPurposeByMonth(major: String?, startMonth: YearMonth?, endMonth: YearMonth?): Flow<PurposeByMonthDto>
    suspend fun getTotalStudentEntries(startDate: LocalDate?, endDate: LocalDate?): TotalStudentAttendByTime

    //borrow
    fun getBorrowDataEachMajor(startDate: LocalDate?, endDate: LocalDate?): Flow<MajorAttendBorrowed>
    fun mostBorrow(startDate: LocalDate?, endDate: LocalDate?): Flow<MostBorrow>
    fun getBorrowAndReturn(startDate: LocalDate?, endDate: LocalDate?): Flow<BorrowReturn>


}