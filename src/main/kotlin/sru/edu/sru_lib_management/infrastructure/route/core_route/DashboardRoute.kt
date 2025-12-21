package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.DashboardHandler
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.core.domain.service.BorrowService
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.core.domain.service.DonationService

@Configuration
class DashboardRoute {
    @Bean
    @FlowPreview
    fun dashboardRoute(dashboardHandler: DashboardHandler): RouterFunction<ServerResponse> = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/dashboard").nest {

            /**
             * Gets comprehensive dashboard data for library overview.
             *
             * Aggregates real-time statistics and analytics from multiple services:
             *
             * **Card Data Analytics** (with percentage change from a previous period):
             * - Today's entries with comparison to yesterday
             * - Today's book borrows with comparison to yesterday
             * - Today's book donations with comparison to yesterday
             * - This month's total entries with comparison to last month
             *
             * **Additional Dashboard Data**:
             * - Available books list ([sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto])
             * - Today's attendance details (latest 10 entries, reversed order)
             * - Weekly visitor statistics by day
             * - Total visitors grouped by major
             *
             * Returns [sru.edu.sru_lib_management.core.domain.dto.dashboard.Dashboard] object containing:
             * - cardData: List of [sru.edu.sru_lib_management.core.domain.dto.dashboard.CardData] - 4 analytics cards with current values and percentage changes
             * - totalMajorVisitor: Visitor counts grouped by major/faculty
             * - weeklyVisitor: [sru.edu.sru_lib_management.core.domain.dto.dashboard.WeeklyVisitor] - Daily counts for the past week
             * - bookAvailable: List of [sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto] or error message
             * - customEntry: List of [sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail] - Today's latest 10 entries (reversed)
             *
             * **Analytics Calculation**:
             * - Period 1: Compares today vs. yesterday
             * - Period 30: Compares this month vs. last month
             * - Percentage formula: ((current - previous) / previous) * 100
             * - Returns [Analyze] with currentValue and percentage
             *
             * **Error Handling**:
             * If any analytics fail, returns [Analyze] with values (-0, -0f) as fallback.
             * This ensures the dashboard always returns data even if some metrics fail.
             *
             * **Data Sources**:
             * - [AttendService.analyticAttend] - Entry analytics (1 day, 30 days)
             * - [BorrowService.analyticBorrow] - Borrow analytics
             * - [DonationService.analyticDonation] - Donation analytics
             * - [BookService.getAvailableBook] - Available books
             * - [AttendService.getAllStudentAttendDetail] - Today's attendance
             * - [AttendService.getWeeklyVisit] - Weekly statistics
             * - [AttendService.getTotalMajorVisit] - Major-wise visitor counts
             *
             * No query parameters are required - all data is calculated for the current date.
             *
             * @see DashboardHandler.dashboard
             * @see sru.edu.sru_lib_management.core.domain.dto.dashboard.Dashboard
             * @see sru.edu.sru_lib_management.core.domain.dto.dashboard.CardData
             * @see Analyze
             * @see sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto
             * @see sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
             * @see sru.edu.sru_lib_management.core.domain.dto.dashboard.WeeklyVisitor
             * @see AttendService.analyticAttend
             * @see BorrowService.analyticBorrow
             * @see DonationService.analyticDonation
             * @see BookService.getAvailableBook
             * @see AttendService.getAllStudentAttendDetail
             * @see AttendService.getWeeklyVisit
             * @see AttendService.getTotalMajorVisit
             */
            GET("") { dashboardHandler.dashboard() }
        }
    }
}