package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.core.domain.dto.DurationSpent
import sru.edu.sru_lib_management.core.domain.dto.PurposeByMonthDto
import sru.edu.sru_lib_management.core.domain.dto.PurposeDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.*
import sru.edu.sru_lib_management.core.domain.repository.AnalyticRepository
import sru.edu.sru_lib_management.core.domain.service.AnalyticService
import java.time.LocalDate
import java.time.YearMonth

/**
 *
 */
@Component
class AnalyticServiceImp(
    private val analyticRepository: AnalyticRepository
): AnalyticService {

    /**
     *
     */
    private val logger = LoggerFactory.getLogger(AnalyticServiceImp::class.java)

    /**
     * Retrieves a stream of book borrowing data grouped by the college for the specified time period.
     *
     * @param startMonth The starting month of the period to query, or null if no lower bound is set.
     * @param endMonth The ending month of the period to query, or null if no upper bound is set.
     * @return Flow emitting `BookEachCollege` objects containing book borrowing data for each college.
     */
    override fun getBookEachCollege(
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<BookEachCollege> {
        return analyticRepository.getBookEachCollege(startMonth, endMonth)
    }

    /**
     * Retrieves the income data related to books (donation and university funding) for a specified time period.
     *
     * @param startMonth The starting month of the period to query income data. Can be null to include data from the earliest available month.
     * @param endMonth The ending month of the period to query income data. Can be null to include data up to the latest available month.
     * @return A [Flow] emitting [BookIncome] objects, each containing income details for a specified month.
     */
    override fun getBookIncome(
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<BookIncome> {
        return analyticRepository.getBookIncome(startMonth, endMonth)
    }

    /**
     * Retrieves the total number of books and their distribution across different languages.
     *
     * @return A [TotalBook] object containing the total count of books and a map of book counts per language.
     */
    override suspend fun getTotalBookEachLanguage(): TotalBook {
        return analyticRepository.getTotalBookEachLanguage()
    }

    /**
     * Retrieves purpose-related analytics based on the specified parameters.
     *
     * @param major The major or department name to filter the results. Can be null to include all majors.
     * @param startDate The start date for the analytics. Can be null to include data from the earliest available date.
     * @param endDate The end date for the analytics. Can be null to include data up to the latest available date.
     * @return A [Flow] emitting [PurposeDto] objects, each containing a purpose type and its associated count.
     */
    override fun getPurposeCount(
        major: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<PurposeDto> {
        return analyticRepository.getPurposeCount(major, startDate, endDate)
    }

    /**
     * Retrieves the total time spent data by students within the specified date range.
     *
     * @param startDate The starting date of the period to query time spent data. Can be null to include data from the earliest available date.
     * @param endDate The ending date of the period to query time spent data. Can be null to include data up to the latest available date.
     * @return A [Flow] emitting [DurationSpent] objects, each containing details about a student's ID, name, major, degree, generation, and total time spent.
     */
    override fun getTimeSpend(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<DurationSpent> {
        return analyticRepository.getTimeSpend(startDate, endDate)
    }

    /**
     * Retrieves attendance statistics by major within a specified date range.
     *
     * @param startDate the starting date of the range; can be null to include all records from the earliest date.
     * @param endDate the ending date of the range; can be null to include all records up to the latest date.
     * @return a [Flow] emitting [MajorAttendBorrowed] instances, each containing details about a major's attendance count, percentage, and name.
     */
    override fun mostMajorAttend(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MajorAttendBorrowed> {
        return analyticRepository.mostMajorAttend(startDate, endDate)
    }

    /**
     * Retrieves purpose-related data grouped by month for the specified range and major.
     *
     * @param major The major or department name to filter data. Can be null to include all majors.
     * @param startMonth The starting month of the period to query. Can be null to include data from the earliest available month.
     * @param endMonth The ending month of the period to query. Can be null to include data up to the latest available month.
     * @return A [Flow] emitting [PurposeByMonthDto] objects, each representing purpose-related data within the specified month.
     */
    override fun getPurposeByMonth(
        major: String?,
        startMonth: YearMonth?,
        endMonth: YearMonth?,
    ): Flow<PurposeByMonthDto> {
        logger.info("Start Month is: " + startMonth?.toString())
        logger.info("End Month is: " + startMonth?.toString())
        return analyticRepository.getPurposeByMonth(major, startMonth, endMonth)
    }

    /**
     * Retrieves the total student attendance data for a specified date range.
     *
     * @param startDate The starting date of the attendance period. Can be null to include all data from the earliest available date.
     * @param endDate The ending date of the attendance period. Can be null to include all data up to the latest available date.
     * @return A [TotalStudentAttendByTime] object containing total attendance counts, gender-based data, and time-based attendance distribution.
     */
    override suspend fun getTotalStudentEntries(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): TotalStudentAttendByTime {
        return analyticRepository.getTotalStudentEntries(startDate, endDate)
    }

    /**
     * Retrieves borrowing data grouped by majors within the specified date range.
     *
     * @param startDate The starting date of the period to filter borrowing data. Can be null to include all records from the earliest date.
     * @param endDate The ending date of the period to filter borrowing data. Can be null to include all records up to the latest date.
     * @return A [Flow] emitting [MajorAttendBorrowed] objects, each containing the major's name, number of borrowings, and the percentage share.
     */
    override fun getBorrowDataEachMajor(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MajorAttendBorrowed> {
        return analyticRepository.getBorrowDataEachMajor(startDate, endDate)
    }

    /**
     * Retrieves the most borrowed books within a specified date range.
     *
     * @param startDate The start date of the period to filter borrowed books. Can be null.
     * @param endDate The end date of the period to filter borrowed books. Can be null.
     * @return A flow of MostBorrow objects, each containing details such as rank, book title, genre, and borrow quantity.
     */
    override fun mostBorrow(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<MostBorrow> {
        return analyticRepository.mostBorrow(startDate, endDate)
    }

    /**
     * Retrieves the borrow and return statistics within a specified date range.
     *
     * @param startDate The start date of the period to fetch data for. Can be null to indicate no lower bound.
     * @param endDate The end date of the period to fetch data for. Can be null to indicate no upper bound.
     * @return A flow emitting borrow and return data containing the month, borrow count, and return count.
     */
    override fun getBorrowAndReturn(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): Flow<BorrowReturn> {
        return analyticRepository.getBorrowAndReturn(startDate, endDate)
    }


}