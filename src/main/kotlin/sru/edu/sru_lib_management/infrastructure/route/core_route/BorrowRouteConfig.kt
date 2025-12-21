package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.BorrowHandler

@Configuration
class BorrowRouteConfig {
    @Bean
    @FlowPreview
    fun borrowRoute(borrowHandler: BorrowHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/borrow".nest {

                /**
                 * Counts borrow transactions per week.
                 *
                 * Returns borrowing statistics for the previous week (Monday to Sunday).
                 * Returns Map<LocalDate, Int> where key is the date and value is the count.
                 *
                 * @see BorrowHandler.countBorrowsPerWeek
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.countBorrowPerWeek
                 */
                GET("/week-count") { borrowHandler.countBorrowsPerWeek() }

                /**
                 * Creates a new borrow record.
                 *
                 * Requires [sru.edu.sru_lib_management.core.domain.dto.BorrowDto] in request body with:
                 * - studentId: Long - Student borrowing the book
                 * - bookId: String - Book being borrowed
                 * - borrowDate: LocalDate - Date of borrowing
                 * - dueDate: LocalDate - Expected return date
                 *
                 * Process:
                 * 1. Validate students and books exist
                 * 2. Checks book availability
                 * 3. Creates borrow record
                 * 4. Updates book quantity (decrements available copies)
                 *
                 * Returns saved [sru.edu.sru_lib_management.core.domain.model.Borrow] entity.
                 * Requires the ADMIN or SUPER_ADMIN role.
                 *
                 * @see BorrowHandler.saveBorrow
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.saveBorrow
                 * @see sru.edu.sru_lib_management.core.domain.dto.BorrowDto
                 * @see sru.edu.sru_lib_management.core.domain.model.Borrow
                 */
                POST("", borrowHandler::saveBorrow)

                /**
                 * Gets all borrow records.
                 *
                 * Returns [kotlinx.coroutines.flow.Flow] of all [sru.edu.sru_lib_management.core.domain.model.Borrow] entities from the database.
                 * Includes both active (unreturned) and completed (returned) borrows.
                 *
                 * @see BorrowHandler.getAllBorrow
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.getBorrows
                 * @see sru.edu.sru_lib_management.core.domain.model.Borrow
                 */
                GET("") { borrowHandler.getAllBorrow() }

                /**
                 * Gets detailed borrow information for all records.
                 *
                 * Returns List of [sru.edu.sru_lib_management.core.domain.dto.BorrowDetail] with enriched data including
                 * - Student information (name, ID, major, etc.)
                 * - Book information (title, author, bookId, etc.)
                 * - Borrow dates (borrowDate, dueDate, returnedDate)
                 * - Status (active/returned/overdue)
                 *
                 * @see BorrowHandler.borrowDetails
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.getBorrowDetail
                 * @see sru.edu.sru_lib_management.core.domain.dto.BorrowDetail
                 */
                GET("/detail") { borrowHandler.borrowDetails() }

                /**
                 * Gets only active (unreturned) borrow records.
                 *
                 * Returns [kotlinx.coroutines.flow.Flow] of [sru.edu.sru_lib_management.core.domain.dto.BorrowDetail] where returnedDate is null.
                 * Shows only books currently borrowed and not yet returned.
                 * Useful for tracking which books are currently out on loan.
                 *
                 * @see BorrowHandler.getActiveBorrow
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.getActiveBorrowed
                 * @see sru.edu.sru_lib_management.core.domain.dto.BorrowDetail
                 */
                GET("/detail-active") { borrowHandler.getActiveBorrow() }

                /**
                 * Gets all overdue books.
                 *
                 * Returns [kotlinx.coroutines.flow.Flow] of [sru.edu.sru_lib_management.core.domain.model.Borrow] entities where:
                 * - returnedDate is null (book not returned yet)
                 * - Current date > dueDate (past the due date)
                 *
                 * Useful for:
                 * - Sending overdue reminders to students
                 * - Calculating late fees
                 * - Generating overdue reports
                 *
                 * @see BorrowHandler.getAllOverDueBooks
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.overDueService
                 * @see sru.edu.sru_lib_management.core.domain.model.Borrow
                 */
                GET("/over-due") { borrowHandler.getAllOverDueBooks() }

                /**
                 * Marks a book as returned.
                 *
                 * Query params (both required):
                 * - studentId: Long - Student ID who borrowed the book
                 * - bookId: String - Book ID being returned
                 *
                 * Process:
                 * 1. Finds active borrow record matching studentId and bookId
                 * 2. Sets returnedDate to the current date
                 * 3. Increments book quantity (makes it available again)
                 * 4. May calculate late fees if overdue
                 *
                 * Returns updated [sru.edu.sru_lib_management.core.domain.model.Borrow] entity with returnedDate set.
                 * Requires the ADMIN or SUPER_ADMIN role.
                 *
                 * @see BorrowHandler.bookReturned
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.bookReturned
                 * @see sru.edu.sru_lib_management.core.domain.model.Borrow
                 */
                PUT("", borrowHandler::bookReturned)

                /**
                 * Extends the due date for a borrowed book.
                 *
                 * Query param:
                 * - id: Long (required) - Borrow record ID to extend
                 *
                 * Extends the dueDate by a predefined period (typically 7-14 days).
                 * Useful when students need more time to complete reading.
                 * Cannot extend if a book is already overdue or already returned.
                 *
                 * Returns updated [sru.edu.sru_lib_management.core.domain.model.Borrow] entity with new dueDate.
                 * Requires the ADMIN or SUPER_ADMIN role.
                 *
                 * @see BorrowHandler.extendBorrowBook
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.extendBorrow
                 * @see sru.edu.sru_lib_management.core.domain.model.Borrow
                 */
                PUT("/extend-borrow", borrowHandler::extendBorrowBook)

                /**
                 * Searches borrow records by keyword.
                 *
                 * Query param:
                 * - keyword: String (required) - Search term
                 *
                 * Searches across multiple fields:
                 * - Student name
                 * - Student ID
                 * - Book title
                 * - Book ID
                 *
                 * Returns list of matching [sru.edu.sru_lib_management.core.domain.dto.BorrowDetail] records.
                 * Search is case-insensitive and partial match.
                 *
                 * Example:
                 * ```
                 * GET /api/v1/borrow/search?keyword=physics
                 * ```
                 *
                 * @see BorrowHandler.searchBorrow
                 * @see sru.edu.sru_lib_management.core.domain.service.BorrowService.searchBorrow
                 * @see sru.edu.sru_lib_management.core.domain.dto.BorrowDetail
                 */
                GET("/search", borrowHandler::searchBorrow)
            }
        }
    }
}