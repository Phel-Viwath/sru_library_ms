/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.infrastructure.route.core_route

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.auth.controller.AuthHandler
import sru.edu.sru_lib_management.core.handler.*

@Configuration
class RouteConfig(
    private val analyticHandler: AnalyticHandler,
    private val authHandler: AuthHandler,
    private val dashboardHandler: DashboardHandler,
    private val studentHandler: StudentHandler,
    private val entryHandler: EntryHandler,
    private val donateHandler: DonateHandler,
    private val bookHandler: BookHandler,
    private val borrowHandler: BorrowHandler,
    private val attendHandler: AttendHandler,
    private val reportHandler: ReportHandler
) {

    ///
    // Auth route
    @Bean
    fun authRoute() = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/v1/auth".nest {
                POST("/register", authHandler::register)
                POST("/login", authHandler::login)
                POST("/refresh-token", authHandler::refresh)
                POST("/otp", authHandler::generateOtp)
                POST("/verify", authHandler::verifyOtp)
                PUT("/change-password", authHandler::changePassword)
                PUT("/change-role", authHandler::changeRole)
                GET("", authHandler::getAllUser)
            }
        }
    }

    /// Core Route
    ///
    @Bean
    fun analyticRoute() = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/v1/analytic".nest {
                GET("", analyticHandler::analytic)
            }
        }
    }

    @Bean
    fun attendRoute() = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "api/v1/att".nest {
                GET("/purpose", attendHandler::getAttendPurpose)
                GET("/time-spent", attendHandler::getDurationSpent)
                GET("/weekly", attendHandler::weeklyVisitor)
                GET("/detail", attendHandler::getDetails)
                GET("/compare", attendHandler::countAndCompare)
                GET("/count", attendHandler::countCustomAttend)
                GET("/custom", attendHandler::getCustomAttend)
                GET("", attendHandler::getAllAttend)
                PUT("", attendHandler::updateExitingTime)
                PUT("/{attId}", attendHandler::updateAtt)
            }
        }
    }

    @Bean
    fun bookRouter(): RouterFunction<*> = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/v1/book".nest {
                POST("", bookHandler::addNewBook)
                GET("", bookHandler::getBooks)
                PUT("", bookHandler::updateBook)
                GET("/current-book", bookHandler::currentAvailableBook)
                GET("/{bookId}", bookHandler::getBookById)
                DELETE("/{bookId}", bookHandler::deleteBook)
                GET("/available", bookHandler::availableBook)
                PUT("/trash", bookHandler::moveToTrash)
                PUT("/recover", bookHandler::recoverBook)
                GET("/in-trash", bookHandler::getBooksInTrash)
                GET("/income", bookHandler::getBookIncome)
                GET("/about-book-data", bookHandler::aboutBookData)
            }
        }
    }

    @Bean
    fun borrowRoute() = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/v1/borrow".nest {
                GET("/week-count", borrowHandler::countBorrowsPerWeek)
                POST("", borrowHandler::saveBorrow)
                GET("", borrowHandler::getAllBorrow)
                GET("/over-due", borrowHandler::getAllOverDueBooks)
                PUT("", borrowHandler::bookReturned)
                PUT("/extend-borrow", borrowHandler::extendBorrowBook)
            }
        }
    }

    @Bean
    fun dashboardRoute(): RouterFunction<ServerResponse> =  coRouter {
        accept(MediaType.APPLICATION_JSON).nest{
            "/api/v1/dashboard".nest {
                GET("", dashboardHandler::dashboard)
            }
        }
    }

    @Bean
    fun donationRoute(): RouterFunction<ServerResponse> {
        return coRouter {
            accept(MediaType.APPLICATION_JSON).nest {
                "/api/v1/donation".nest {
                    POST("", donateHandler::saveDonation)
                    GET("", donateHandler::donationDetailInfo)
                    PUT("", donateHandler::updateDonation)
                }
            }
        }
    }

    @Bean
    fun entryRoute(): RouterFunction<ServerResponse> = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/v1/entry".nest {
                GET("", entryHandler::recentEntryData)
                GET("/check", entryHandler::checkExistingStudent)
                GET("/{id}", entryHandler::getStudentById)
                POST("", entryHandler::newEntry)
                PUT("", entryHandler::updateExitingTime)
            }
        }
    }

    @Bean
    fun studentRoute(): RouterFunction<ServerResponse> = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "api/v1/student".nest {
                GET("", studentHandler::getAllStudents)
                GET("/{studentId}", studentHandler::getStudentById)
                POST("", studentHandler::saveStudent)
                DELETE("/delete/{studentId}", studentHandler::deleteStudent)
                PUT("/{studentId}", studentHandler::updateStudent)
            }
        }
    }
    @Bean
    fun reportRoute(): RouterFunction<ServerResponse> = coRouter {
        accept(MediaType.APPLICATION_JSON).nest {
            "/api/v1/report".nest {
                GET("", reportHandler::report)
            }
        }
    }



}