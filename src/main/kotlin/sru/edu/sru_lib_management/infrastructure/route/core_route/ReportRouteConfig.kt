package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto
import sru.edu.sru_lib_management.core.handler.ReportHandler
import sru.edu.sru_lib_management.core.domain.service.ReportService
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.core.domain.service.StaffService

@Configuration
class ReportRouteConfig {
    @Bean
    @FlowPreview
    fun reportRoute(reportHandler: ReportHandler): RouterFunction<ServerResponse> = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/report").nest {

            /**
             * Generates the comprehensive library report for a specified period.
             *
             * Aggregates multiple data sources into a single report including
             * - Total books in the library grouped by language
             * - Books distributed across different colleges/faculties
             * - Student monthly attendance entries
             * - Staff monthly attendance entries
             * - Book donations received during the period
             * - Active library staff members
             *
             * Query params:
             * - startMonth: YearMonth (required, format: YYYY-MM) - Start month of report period
             * - endMonth: YearMonth (required, format: YYYY-MM) - End month of report period
             *
             * Returns [sru.edu.sru_lib_management.core.domain.dto.report.Report] object containing:
             * - bookEachCollege: [sru.edu.sru_lib_management.core.domain.dto.analytic.BookEachCollege] list - Books per college/faculty
             * - studentMonthlyEntry: [sru.edu.sru_lib_management.core.domain.dto.report.MonthlyEntry]
             * - staffMonthlyEntry: [sru.edu.sru_lib_management.core.domain.dto.report.MonthlyEntry]
             * - totalBookInLibrary: Map<String, Int> - Total books grouped by languageId
             * - listOfDonation: [sru.edu.sru_lib_management.core.domain.model.Donation] list - All donations during the period
             * - libraryStaff: [StaffDto] list - Active library staff members
             *
             * Process flow:
             * 1. Converts YearMonth params to LocalDate range (first day to last day)
             * 2. Fetches all books and groups by language, summing quantities
             * 3. Retrieves staff and student attendance via [sru.edu.sru_lib_management.core.domain.service.ReportService]
             * 4. Gets active library staff from [sru.edu.sru_lib_management.core.domain.service.StaffService]
             * 5. Fetches book data per college from [sru.edu.sru_lib_management.core.domain.service.BookService]
             * 6. Collects donation records for the period
             * 7. Aggregates all data into [sru.edu.sru_lib_management.core.domain.dto.report.Report] object
             *
             * Requires SUPER_ADMIN role only.
             *
             * Example request:
             * ```
             * GET /api/v1/report?startMonth=2024-01&endMonth=2024-12
             * ```
             *
             * @see ReportHandler.report
             * @see sru.edu.sru_lib_management.core.domain.dto.report.Report
             * @see sru.edu.sru_lib_management.core.domain.service.ReportService.staffAttendList
             * @see ReportService.studentAttendList
             * @see ReportService.getAllDonation
             * @see BookService.getAllBooks
             * @see BookService.getBookDataForEachCollege
             * @see StaffService.findAll
             * @see sru.edu.sru_lib_management.core.domain.dto.analytic.BookEachCollege
             * @see sru.edu.sru_lib_management.core.domain.dto.report.MonthlyEntry
             * @see sru.edu.sru_lib_management.core.domain.model.Donation
             * @see StaffDto
             */
            GET("", reportHandler::report)
        }
    }
}