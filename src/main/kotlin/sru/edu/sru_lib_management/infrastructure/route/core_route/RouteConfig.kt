/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.auth.controller.AuthHandler
import sru.edu.sru_lib_management.core.handler.*

@Configuration
class RouteConfig {
    // Auth route

    @Bean
    fun helloRoute(welcomeHandler: WelcomeHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/welcome".nest {
                GET(""){welcomeHandler.hello()}
            }
        }
    }

    @Bean
    fun authRoute(authHandler: AuthHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/auth".nest {
                POST("/register", authHandler::register)
                POST("/login", authHandler::login)
                POST("/refresh-token", authHandler::refresh)
                POST("/otp", authHandler::generateOtp)
                POST("/verify", authHandler::verifyOtp)
                PUT("/change-password", authHandler::changePassword)
                PUT("/change-role", authHandler::changeRole)
                GET(""){authHandler.getAllUser()}
            }
        }
    }

    /// Core Route
    ///
    @FlowPreview
    @Bean
    fun analyticRoute(analyticHandler: AnalyticHandler) = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/analytic").nest {
            GET("", analyticHandler::analytic)
        }
    }

    @Bean
    @FlowPreview
    fun attendRoute(attendHandler: AttendHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            "api/v1/att".nest {
                GET("/purpose", attendHandler::getAttendPurpose)
                GET("/time-spent"){attendHandler.getDurationSpent()}
                GET("/weekly"){attendHandler.weeklyVisitor()}
                GET("/detail"){attendHandler.getDetails()}
                GET("/compare", attendHandler::countAndCompare)
                GET("/count", attendHandler::countCustomAttend)
                GET("/custom", attendHandler::getCustomAttend)
                GET(""){attendHandler.getAllAttend()}
                PUT("", attendHandler::updateExitingTime)
                PUT("/{attId}", attendHandler::updateAtt)
            }
        }
    }

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


    @Bean
    @FlowPreview
    fun borrowRoute(borrowHandler: BorrowHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/borrow".nest {
                GET("/week-count"){borrowHandler.countBorrowsPerWeek()}
                POST("", borrowHandler::saveBorrow)
                GET(""){borrowHandler.getAllBorrow()}
                GET("/detail"){borrowHandler.borrowDetails()}
                GET("/detail-active"){borrowHandler.getActiveBorrow()}
                GET("/over-due"){borrowHandler.getAllOverDueBooks()}
                PUT("", borrowHandler::bookReturned)
                PUT("/extend-borrow", borrowHandler::extendBorrowBook)
                GET("/search", borrowHandler::searchBorrow)
            }
        }
    }

    @Bean
    @FlowPreview
    fun dashboardRoute(dashboardHandler: DashboardHandler): RouterFunction<ServerResponse> =  coRouter {
        accept(APPLICATION_JSON).nest{
            "/api/v1/dashboard".nest {
                GET(""){dashboardHandler.dashboard()}
            }
        }
    }

    @Bean
    @FlowPreview
    fun donationRoute(donateHandler: DonateHandler): RouterFunction<ServerResponse> {
        return coRouter {
            accept(APPLICATION_JSON).nest {
                "/api/v1/donation".nest {
                    POST("", donateHandler::saveDonation)
                    GET(""){donateHandler.donationDetailInfo()}
                    PUT("", donateHandler::updateDonation)
                }
            }
        }
    }

    @Bean
    @FlowPreview
    fun entryRoute(entryHandler: EntryHandler): RouterFunction<ServerResponse> = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/entry".nest {
                GET(""){entryHandler.recentEntryData()}
                GET("/check", entryHandler::checkExistingStudent)
                GET("/{id}", entryHandler::getStudentById)
                POST("", entryHandler::newEntry)
                PUT("", entryHandler::updateExitingTime)
            }
        }
    }

    @Bean
    @FlowPreview
    fun studentRoute(studentHandler: StudentHandler): RouterFunction<ServerResponse> = coRouter {
        accept(APPLICATION_JSON).nest {
            "api/v1/student".nest {
                GET(""){studentHandler.getAllStudents()}
                GET("/{studentId}", studentHandler::getStudentById)
                POST("", studentHandler::saveStudent)
                DELETE("/delete/{studentId}", studentHandler::deleteStudent)
                PUT("/{studentId}", studentHandler::updateStudent)
            }
        }
    }
    @Bean
    @FlowPreview
    fun reportRoute(reportHandler: ReportHandler): RouterFunction<ServerResponse> = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/report".nest {
                GET("", reportHandler::report)
            }
        }
    }

    @Bean
    fun uploadRoute(bookHandler: BookHandler): RouterFunction<ServerResponse> = coRouter {
        accept(MediaType.MULTIPART_FORM_DATA).nest{
            "/api/v1/upload".nest {
                POST("/book", bookHandler::uploadBook)
            }
        }
    }

}