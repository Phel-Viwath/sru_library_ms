package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.flow.Flow
import org.apache.logging.log4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
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
import sru.edu.sru_lib_management.core.domain.repository.AnalyticRepository
import sru.edu.sru_lib_management.core.domain.service.AnalyticService
import java.time.LocalDate
import java.time.YearMonth

@Component
class AnalyticServiceImp(
    private val analyticRepository: AnalyticRepository
): AnalyticService {

    private val logger = LoggerFactory.getLogger(AnalyticServiceImp::class.java)

    override fun getBookEachCollege(
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<BookEachCollege> {
        return analyticRepository.getBookEachCollege(startMonth, endMonth)
    }

    override fun getBookIncome(
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<BookIncome> {
        return analyticRepository.getBookIncome(startMonth, endMonth)
    }

    override suspend fun getTotalBookEachLanguage(): TotalBook {
        return analyticRepository.getTotalBookEachLanguage()
    }

    override fun getPurposeCount(
        major: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<PurposeDto> {
        return analyticRepository.getPurposeCount(major, startDate, endDate)
    }

    override fun getTimeSpend(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<DurationSpent> {
        return analyticRepository.getTimeSpend(startDate, endDate)
    }

    override fun mostMajorAttend(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MajorAttendBorrowed> {
        return analyticRepository.mostMajorAttend(startDate, endDate)
    }

    override fun getPurposeByMonth(
        major: String?,
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<PurposeByMonthDto> {
        logger.info("Start Month is: " + startMonth?.toString())
        logger.info("End Month is: " + startMonth?.toString())
        return analyticRepository.getPurposeByMonth(major, startMonth, endMonth)
    }

    override suspend fun getTotalStudentEntries(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): TotalStudentAttendByTime {
        return analyticRepository.getTotalStudentEntries(startDate, endDate)
    }

    override fun getBorrowDataEachMajor(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MajorAttendBorrowed> {
        return analyticRepository.getBorrowDataEachMajor(startDate, endDate)
    }

    override fun mostBorrow(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MostBorrow> {
        return analyticRepository.mostBorrow(startDate, endDate)
    }

    override fun getBorrowAndReturn(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<BorrowReturn> {
        return analyticRepository.getBorrowAndReturn(startDate, endDate)
    }


}