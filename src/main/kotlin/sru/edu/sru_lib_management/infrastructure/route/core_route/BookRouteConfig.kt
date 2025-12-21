package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.BookHandler

@Configuration
class BookRouteConfig {
    @Bean
    @FlowPreview
    fun bookRouter(bookHandler: BookHandler): RouterFunction<ServerResponse> = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/book").nest {
            /**
             * Adds new books to the database.
             * Requires a list of [sru.edu.sru_lib_management.core.domain.dto.BookDto] as request body.
             * @see BookHandler.addNewBook
             */
            POST("", bookHandler::addNewBook)

            /**
             * Gets all books from the database.
             * Returns [kotlinx.coroutines.flow.Flow] of [sru.edu.sru_lib_management.core.domain.model.Books].
             * @see BookHandler.getBooks
             */
            GET("") { bookHandler.getBooks() }

            /**
             * Updates an existing book in the database.
             * Requires [sru.edu.sru_lib_management.core.domain.dto.BookDto] with bookId as request body.
             * @see BookHandler.updateBook
             */
            PUT("", bookHandler::updateBook)

            /**
             * Gets all available books.
             * Returns list of [sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto].
             * @see BookHandler.availableBook
             */
            GET("/available") { bookHandler.availableBook() }

            /**
             * Moves books to trash (soft delete).
             * Query param: bookId (can be multiple).
             * @see BookHandler.moveToTrash
             */
            PUT("/trash", bookHandler::moveToTrash)

            /**
             * Recovers books from trash.
             * Query param: bookId (can be multiple).
             * @see BookHandler.recoverBook
             */
            PUT("/recover", bookHandler::recoverBook)

            /**
             * Gets all books in Trash.
             * Returns [kotlinx.coroutines.flow.Flow] of [sru.edu.sru_lib_management.core.domain.dto.BookDto].
             * @see BookHandler.getBooksInTrash
             */
            GET("/in-trash") { bookHandler.getBooksInTrash() }

            /**
             * Gets book income statistics by date range.
             * Query params: sYearMonth, eYearMonth (format: YYYY-MM).
             * @see BookHandler.getBookIncome
             */
            GET("/income", bookHandler::getBookIncome)

            /**
             * Gets book statistics and metadata.
             * Returns Map<String, Int> with book data.
             * @see BookHandler.aboutBookData
             */
            GET("/about-book-data") { bookHandler.aboutBookData() }

            /**
             * Searches books by keyword.
             * Query param: keyword (required).
             * Returns [kotlinx.coroutines.flow.Flow] of [sru.edu.sru_lib_management.core.domain.model.Books] matching the search.
             * @see BookHandler.searchBook
             */
            GET("/search") { bookHandler.searchBook(it) }

            /**
             * Gets currently available books.
             * Returns [kotlinx.coroutines.flow.Flow] of [sru.edu.sru_lib_management.core.domain.model.Books] that are currently available.
             * @see BookHandler.currentAvailableBook
             */
            GET("/current-book") { bookHandler.currentAvailableBook() }

            /**
             * Gets a specific book by ID.
             * Path param: bookId.
             * Returns single [sru.edu.sru_lib_management.core.domain.model.Books] entity.
             * @see BookHandler.getBookById
             */
            GET("/{bookId}", bookHandler::getBookById)

            /**
             * Permanently deletes a book from the database.
             * Path param: bookId.
             * Requires SUPER_ADMIN role.
             * @see BookHandler.deleteBook
             */
            DELETE("/{bookId}", bookHandler::deleteBook)

        }
    }


}