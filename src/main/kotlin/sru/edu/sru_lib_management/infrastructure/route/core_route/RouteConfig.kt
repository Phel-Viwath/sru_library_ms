/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.router
import sru.edu.sru_lib_management.auth.controller.AuthHandler
import sru.edu.sru_lib_management.core.handler.*

@Configuration
class RouteConfig {
    // Auth route

    @Bean
    fun helloRoute(welcomeHandler: WelcomeHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/welcome".nest {
                GET("", welcomeHandler::hello)
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
                GET("", authHandler::getAllUser)
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
    @FlowPreview
    fun bookRouter(bookHandler: BookHandler): RouterFunction<ServerResponse> = coRouter {
        (accept(APPLICATION_JSON) and "/api/v1/book").nest {
            POST("", bookHandler::addNewBook)
            GET("", bookHandler::getBooks)
            PUT("", bookHandler::updateBook)

            GET("/available", bookHandler::availableBook)
            PUT("/trash", bookHandler::moveToTrash)
            PUT("/recover", bookHandler::recoverBook)
            GET("/in-trash", bookHandler::getBooksInTrash)
            GET("/income", bookHandler::getBookIncome)
            GET("/about-book-data",bookHandler::aboutBookData)

            GET("/current-book", bookHandler::currentAvailableBook)
            GET("/{bookId}", bookHandler::getBookById)
            DELETE("/{bookId}", bookHandler::deleteBook)
        }
    }


    @Bean
    @FlowPreview
    fun borrowRoute(borrowHandler: BorrowHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
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
    @FlowPreview
    fun dashboardRoute(dashboardHandler: DashboardHandler): RouterFunction<ServerResponse> =  coRouter {
        accept(APPLICATION_JSON).nest{
            "/api/v1/dashboard".nest {
                GET("", dashboardHandler::dashboard)
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
                    GET("", donateHandler::donationDetailInfo)
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
                GET("", entryHandler::recentEntryData)
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
                GET("", studentHandler::getAllStudents)
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



}