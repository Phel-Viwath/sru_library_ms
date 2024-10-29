/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.flow.toList
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import sru.edu.sru_lib_management.core.domain.dto.analytic.Analytic
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.core.domain.service.BorrowService
import java.time.LocalDate
import java.time.YearMonth

@Component
class AnalyticHandler(
    private val bookService: BookService,
    private val borrowService: BorrowService,
    private val attendService: AttendService
) {

    @PreAuthorize("hasRole('USER')")
    suspend fun analytic(request: ServerRequest): ServerResponse{

        val startDate = request.queryParam("startDate")
            .map { LocalDate.parse(it) }
            .orElse(null)

        val endDate = request.queryParam("endDate")
            .map { LocalDate.parse(it) }
            .orElse(null)

        val major = request.queryParamOrNull("major")


        val startMonth = YearMonth.from(startDate)
        val endMonth = YearMonth.from(endDate)

        val purposeCount = attendService.getPurposes(major, startDate, endDate)
        val bookEachCollege = bookService.getBookDataForEachCollege(startMonth, endMonth)
        val totalBook = bookService.bookLanguage()
        val timeSpent = attendService.countDuration(startDate, endDate).toList()
        val majorBorrows = borrowService.getBorrowDataEachMajor(startDate, endDate)
        val mostBorrows = borrowService.mostBorrow(startDate, endDate).toList()
        val mostMajorAttend = attendService.getMostAttend(startDate, endDate)
        val getTotalStudentEntries = attendService.countAttendByOpenTime(startDate, endDate)
        val bookIncome = bookService.getBookIncome(startMonth, endMonth)
        val getPurposeByMonthDto = attendService.getPurposeByMonth(major, startMonth, endMonth)
        val borrowAndReturn = borrowService.getBorrowAndReturn(startDate, endDate)

        val analytic =  Analytic(
            bookIncome = bookIncome,
            purposeCount = purposeCount,
            totalBook = totalBook,
            timeSpent = timeSpent.take(10),
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