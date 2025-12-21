package sru.edu.sru_lib_management.infrastructure.route.core_route

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.AnalyticHandler

@Configuration
class AnalyticRouteConfig {
    @Bean
    fun analyticRoute(analyticHandler: AnalyticHandler) = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/analytic").nest {
            /**
             * Gets comprehensive analytics data for library operations.
             *
             * Aggregates multiple analytics including
             * - Book income and collection statistics by college
             * - Student attendance purposes and time spent
             * - Major-wise attendance and borrowing trends
             * - Most borrowed books and return statistics
             *
             * Query params:
             * - startDate: LocalDate (required, format: YYYY-MM-DD) - Start date for analytics period
             * - endDate: LocalDate (required, format: YYYY-MM-DD) - End date for analytics period
             * - major: String (optional) - Filter by specific major name
             *
             * Returns [sru.edu.sru_lib_management.core.domain.dto.analytic.Analytic] object containing:
             * - [sru.edu.sru_lib_management.core.domain.dto.analytic.BookIncome] list - Book income by month
             * - [sru.edu.sru_lib_management.core.domain.dto.PurposeDto] list - Purpose counts (Reading, Assignment, Use PC, Other)
             * - [sru.edu.sru_lib_management.core.domain.dto.analytic.TotalBook] - Total books
             * - [sru.edu.sru_lib_management.core.domain.dto.DurationSpent] list - Time spent by students in library
             * - [sru.edu.sru_lib_management.core.domain.dto.analytic.MajorAttendBorrowed] list - Attendance by major
             * - [sru.edu.sru_lib_management.core.domain.dto.analytic.MostBorrow] list - Most borrowed books
             * - [sru.edu.sru_lib_management.core.domain.dto.analytic.BookEachCollege] list - Books per college/faculty
             * - [sru.edu.sru_lib_management.core.domain.dto.analytic.TotalStudentAttendByTime] - Student entries by time period (morning/afternoon/evening)
             * - [sru.edu.sru_lib_management.core.domain.dto.PurposeByMonthDto] list - Purpose breakdown by month
             * - [sru.edu.sru_lib_management.core.domain.dto.analytic.BorrowReturn] list - Borrow and return statistics
             *
             * Requires the ADMIN or SUPER_ADMIN role.
             *
             * @see AnalyticHandler.analytic
             * @see sru.edu.sru_lib_management.core.domain.dto.analytic.Analytic
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getBookEachCollege
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getTotalBookEachLanguage
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getBookIncome
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getPurposeCount
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getTimeSpend
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.mostMajorAttend
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getTotalStudentEntries
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getPurposeByMonth
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getBorrowDataEachMajor
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.mostBorrow
             * @see sru.edu.sru_lib_management.core.domain.service.AnalyticService.getBorrowAndReturn
             */
            GET("", analyticHandler::analytic)
        }
    }
}