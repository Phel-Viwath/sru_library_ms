/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import sru.edu.sru_lib_management.core.domain.dto.report.Report
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.core.domain.service.DonationService
import sru.edu.sru_lib_management.core.domain.service.ReportService
import sru.edu.sru_lib_management.core.domain.service.StaffService
import sru.edu.sru_lib_management.utils.ResponseStatus.OK
import java.time.YearMonth

@Component
class ReportHandler (
    private val bookService: BookService,
    private val reportService: ReportService,
    private val staffService: StaffService,
) {

    private val logger = LoggerFactory.getLogger(ReportHandler::class.java)

    @GetMapping
    suspend fun report(
        request: ServerRequest
    ): ServerResponse{

        val startMonth = request.queryParam("startMonth")
            .map { YearMonth.parse(it) }
            .orElse(null)

        val endMonth = request.queryParam("endMonth")
            .map { YearMonth.parse(it) }
            .orElse(null)

        val startDate = startMonth?.atDay(1)
        val endDate = endMonth?.atEndOfMonth()

        val book = bookService.getAllBooks()
            .toList()
            .groupBy { it.languageId } // group by languageId
            .mapValues { (_, listBookDto) -> // For each group (languageId) calculate the sum of bookQuan
                listBookDto.sumOf { it.bookQuan }
            }


        val staffEntry = reportService.staffAttendList(startMonth, endMonth)
        val studentEntry = reportService.studentAttendList(startMonth, endMonth)
        val libraryStaffList = staffService.findAll().collectList().awaitSingle()?.filter { it.isActive } ?: emptyList()
        val bookEachCollege = bookService.getBookDataForEachCollege(null, null)

        val report = Report(
            bookEachCollege = bookEachCollege,
            studentMonthlyEntry = studentEntry.toList(),
            staffMonthlyEntry = staffEntry,
            totalBookInLibrary = book,
            listOfDonation = reportService.getAllDonation(startDate, endDate).toList(),
            libraryStaff = libraryStaffList
        )
        return ServerResponse.status(OK).bodyValueAndAwait(report)
    }

}