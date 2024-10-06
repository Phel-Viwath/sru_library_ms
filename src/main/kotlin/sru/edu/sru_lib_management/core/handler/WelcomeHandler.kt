package sru.edu.sru_lib_management.core.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.json

@Component
class WelcomeHandler {
    suspend fun hello(request: ServerRequest): ServerResponse{
        return ServerResponse.ok().json().bodyValueAndAwait("Hello")
    }
}