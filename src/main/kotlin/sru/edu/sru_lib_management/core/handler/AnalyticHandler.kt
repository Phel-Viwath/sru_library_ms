/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import sru.edu.sru_lib_management.core.domain.dto.analytic.Analytic
import sru.edu.sru_lib_management.core.domain.service.AnalyticService
import java.time.LocalDate
import java.time.YearMonth

@Component
class AnalyticHandler(
    private val analyticService: AnalyticService
) {

    private val logger = LoggerFactory.getLogger(AnalyticHandler::class.java)

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun analytic(request: ServerRequest): ServerResponse{

        val startDate = request.queryParam("startDate")
            .map { LocalDate.parse(it) }
            .orElse(null)
            ?: return ServerResponse.badRequest().bodyValueAndAwait("Required parameter startDate is missing")


        val endDate = request.queryParam("endDate")
            .map { LocalDate.parse(it) }
            .orElse(null)
            ?: return ServerResponse.badRequest().bodyValueAndAwait("Required parameter endDate is missing")

        val major = request.queryParamOrNull("major")


        val startMonth = YearMonth.from(startDate)
        val endMonth = YearMonth.from(endDate)

        logger.info("Start month is ${startMonth?.toString()}")
        logger.info("End month is ${endMonth?.toString()}")



        val bookEachCollege = analyticService.getBookEachCollege(startMonth, endMonth).toList()
        val totalBook = analyticService.getTotalBookEachLanguage()
        val bookIncome = analyticService.getBookIncome(startMonth, endMonth).toList()

        val purposeCount = analyticService.getPurposeCount(major, startDate, endDate).toList()
        val timeSpent = analyticService.getTimeSpend(startDate, endDate).toList()
        val mostMajorAttend = analyticService.mostMajorAttend(startDate, endDate).toList()
        val getTotalStudentEntries = analyticService.getTotalStudentEntries(startDate, endDate)
        val getPurposeByMonthDto = analyticService.getPurposeByMonth(major, startMonth, endMonth).toList()

        val majorBorrows = analyticService.getBorrowDataEachMajor(startDate, endDate).toList()
        val mostBorrows = analyticService.mostBorrow(startDate, endDate).toList()
        val borrowAndReturn = analyticService.getBorrowAndReturn(startDate, endDate).toList()

        val analytic =  Analytic(
            bookIncome = bookIncome,
            purposeCount = purposeCount,
            totalBook = totalBook,
            timeSpent = timeSpent,
            mostMajorBorrows = majorBorrows,
            mostBorrowBook = mostBorrows,
            bookEachCollege = bookEachCollege,
            mostMajorAttend = mostMajorAttend,
            studentEntryByTime = getTotalStudentEntries,
            getPurpose = getPurposeByMonthDto,
            borrowAndReturned = borrowAndReturn
        )

        return ServerResponse.ok().bodyValueAndAwait(analytic)
    }

}