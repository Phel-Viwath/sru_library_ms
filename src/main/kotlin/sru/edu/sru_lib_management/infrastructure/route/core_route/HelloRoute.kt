/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.infrastructure.route.core_route

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.*

@Configuration
class HelloRoute {
    // Auth route

    @Bean
    fun helloRoute(welcomeHandler: WelcomeHandler) = coRouter {
        accept(APPLICATION_JSON).nest {
            "/api/v1/welcome".nest {
                GET(""){welcomeHandler.hello()}
            }
        }
    }



}