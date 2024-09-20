/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.dashbord.CardData
import sru.edu.sru_lib_management.core.domain.dto.dashbord.Dashboard
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.core.domain.service.BorrowService
import sru.edu.sru_lib_management.core.domain.service.DonationService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate

@Component
class DashboardHandler(
    private val bookService: BookService,
    private val attendService: AttendService,
    private val borrowService: BorrowService,
    private val donationService: DonationService,
){
    private var card: List<CardData>? = null

    private val logger = LoggerFactory.getLogger(DashboardHandler::class.java)

    //http://localhost:8090/api/v1/dashboard
    //@GetMapping
    //@PreAuthorize("hasRole('USER')")
    suspend fun dashboard(request: ServerRequest): ServerResponse {
        return coroutineScope {
            val entryToday: Analyze = when(val result = attendService.analyticAttend(indoChinaDate(), 1)){
                is CoreResult.Success -> result.data
                is CoreResult.Failure -> Analyze(-0, -0f)
                is CoreResult.ClientError -> Analyze(-0, -0f)
            }
            val entryThisMonth: Analyze = when(val result = attendService.analyticAttend(indoChinaDate(), 30)){
                is CoreResult.Success -> result.data
                is CoreResult.Failure -> Analyze(-0, -0f)
                is CoreResult.ClientError -> Analyze(-0, -0f)
            }
            val borrowToday: Analyze = when(val result = borrowService.analyticBorrow(indoChinaDate(), 1)){
                is CoreResult.Success -> result.data
                is CoreResult.Failure -> Analyze(-0, -0f)
                is CoreResult.ClientError -> Analyze(-0, -0f)
            }

            val sponsorToday: Analyze = when(val result = donationService.analyticDonation(indoChinaDate(), 1)){
                is CoreResult.Success -> result.data
                is CoreResult.Failure -> Analyze(-0, -0f)
                is CoreResult.ClientError -> Analyze(-0, -0f)
            }

            val bookAvailable: Any = when(val result = bookService.getAvailableBook()){
                is CoreResult.Success -> result.data
                is CoreResult.Failure -> result.errorMsg
                is CoreResult.ClientError -> result.clientErrMsg
            }

            val customEntry: List<StudentAttendDetail> = attendService.getAllStudentAttendDetail(indoChinaDate()).toList()


            val weeklyVisitor: Any = when(val result = attendService.getWeeklyVisit()){
                is CoreResult.Success -> result.data
                is CoreResult.Failure -> result.errorMsg
                is CoreResult.ClientError -> result.clientErrMsg
            }
            val totalMajorVisitor: Any = when(val result = attendService.getTotalMajorVisit()){
                is CoreResult.Success -> result.data
                is CoreResult.Failure -> result.errorMsg
                is CoreResult.ClientError -> result.clientErrMsg
            }
            /// Card
            card = listOf(
                CardData("Entry", entryToday.currentValue, entryToday.percentage),
                CardData("Book Borrow", borrowToday.currentValue, borrowToday.percentage),
                CardData("Book Donation", sponsorToday.currentValue, sponsorToday.percentage),
                CardData("Total Entry Of This Month", entryThisMonth.currentValue, entryThisMonth.percentage)
            )
            val response =  Dashboard(
                cardData = card!!,
                totalMajorVisitor = totalMajorVisitor,
                weeklyVisitor = weeklyVisitor,
                bookAvailable = bookAvailable,
                customEntry = customEntry.take(10).reversed()
            )
            ServerResponse.ok().bodyValue(response).awaitSingle()
        }

    }

}