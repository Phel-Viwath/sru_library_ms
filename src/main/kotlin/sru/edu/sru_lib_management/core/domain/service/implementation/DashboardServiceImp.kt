package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.dashboard.CardData
import sru.edu.sru_lib_management.core.domain.dto.dashboard.Dashboard
import sru.edu.sru_lib_management.core.domain.service.AttendService
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.core.domain.service.BorrowService
import sru.edu.sru_lib_management.core.domain.service.DashboardService
import sru.edu.sru_lib_management.core.domain.service.DonationService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate

@Component
class DashboardServiceImp(
    private val bookService: BookService,
    private val attendService: AttendService,
    private val borrowService: BorrowService,
    private val donationService: DonationService
): DashboardService {
    /**
     * Fetches comprehensive dashboard data for library overview.
     *
     * Aggregates real-time statistics and analytics from multiple services:
     * - Today's entries with comparison to yesterday
     * - Today's book borrows with comparison to yesterday
     * - Today's book donations with comparison to yesterday
     * - This month's total entries with comparison to last month
     * - Available books list
     * - Today's attendance details (latest 10 entries, reversed order)
     * - Weekly visitor statistics by day
     * - Total visitors grouped by major
     *
     * @return Dashboard object containing all aggregated data
     */
    override suspend fun getDashboardData(): Dashboard {
        val entryToday: Analyze = when (val result = attendService.analyticAttend(indoChinaDate(), 1)) {
            is CoreResult.Success -> result.data
            is CoreResult.Failure -> Analyze(-0, -0f)
            is CoreResult.ClientError -> Analyze(-0, -0f)
        }

        val entryThisMonth: Analyze = when (val result = attendService.analyticAttend(indoChinaDate(), 30)) {
            is CoreResult.Success -> result.data
            is CoreResult.Failure -> Analyze(-0, -0f)
            is CoreResult.ClientError -> Analyze(-0, -0f)
        }

        val borrowToday: Analyze = when (val result = borrowService.analyticBorrow(indoChinaDate(), 1)) {
            is CoreResult.Success -> result.data
            is CoreResult.Failure -> Analyze(-0, -0f)
            is CoreResult.ClientError -> Analyze(-0, -0f)
        }

        val sponsorToday: Analyze = when (val result = donationService.analyticDonation(indoChinaDate(), 1)) {
            is CoreResult.Success -> result.data
            is CoreResult.Failure -> Analyze(-0, -0f)
            is CoreResult.ClientError -> Analyze(-0, -0f)
        }

        val bookAvailable: Any = when (val result = bookService.getAvailableBook()) {
            is CoreResult.Success -> result.data
            is CoreResult.Failure -> result.errorMsg
            is CoreResult.ClientError -> result.clientErrMsg
        }

        val customEntry: List<StudentAttendDetail> = attendService.getAllStudentAttendDetail(indoChinaDate(), 10).toList().reversed()

        val weeklyVisitor: Any = when (val result = attendService.getWeeklyVisit()) {
            is CoreResult.Success -> result.data
            is CoreResult.Failure -> result.errorMsg
            is CoreResult.ClientError -> result.clientErrMsg
        }

        val totalMajorVisitor: Any = when (val result = attendService.getTotalMajorVisit()) {
            is CoreResult.Success -> result.data
            is CoreResult.Failure -> result.errorMsg
            is CoreResult.ClientError -> result.clientErrMsg
        }

        val cardData = listOf(
            CardData("Entry", entryToday.currentValue, entryToday.percentage),
            CardData("Book Borrow", borrowToday.currentValue, borrowToday.percentage),
            CardData("Book Donation", sponsorToday.currentValue, sponsorToday.percentage),
            CardData("Total Entry Of This Month", entryThisMonth.currentValue, entryThisMonth.percentage)
        )

        return Dashboard(
            cardData = cardData,
            totalMajorVisitor = totalMajorVisitor,
            weeklyVisitor = weeklyVisitor,
            bookAvailable = bookAvailable,
            customEntry = customEntry.take(10).reversed()
        )
    }

}