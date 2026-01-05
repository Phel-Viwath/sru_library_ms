package sru.edu.sru_lib_management.infrastructure.websocket.notification

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import sru.edu.sru_lib_management.auth.domain.jwt.JwtToken
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.auth.domain.repository.AuthRepository
import sru.edu.sru_lib_management.core.domain.model.Notification
import sru.edu.sru_lib_management.infrastructure.websocket.AuthenticatedWebSocketHandler
import sru.edu.sru_lib_management.infrastructure.websocket.helper.WebSocketSessionRegistry

@Component
class NotificationWebSocketHandler(
    jwtToken: JwtToken,
    reactiveUserDetailsService: ReactiveUserDetailsService,
    authRepository: AuthRepository<User>,
    private val sessionRegistry: WebSocketSessionRegistry,
    private val objMapper: ObjectMapper
) : AuthenticatedWebSocketHandler(jwtToken, reactiveUserDetailsService, authRepository) {

    private val sink: Sinks.Many<String> =
        Sinks.many().multicast().onBackpressureBuffer()

    override fun handleAuthenticatedSession(
        session: WebSocketSession,
        userId: String
    ): Mono<Void> {

        return session.send(
            sink.asFlux().map(session::textMessage)
        ).and(
            session.receive()
                .doFinally { sessionRegistry.unregister(userId) }
        )
    }

    fun send(notification: Notification) {
        val json = objMapper.writeValueAsString(notification)
        sink.tryEmitNext(json)
    }
}