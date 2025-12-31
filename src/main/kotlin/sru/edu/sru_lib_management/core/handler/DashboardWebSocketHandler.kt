package sru.edu.sru_lib_management.core.handler

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import sru.edu.sru_lib_management.auth.domain.jwt.JwtToken
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.auth.domain.repository.AuthRepository
import sru.edu.sru_lib_management.core.domain.service.DashboardService
import sru.edu.sru_lib_management.infrastructure.websocket.AuthenticatedWebSocketHandler

@Component
class DashboardWebSocketHandler(
    jwtToken: JwtToken,
    reactiveUserDetailsService: ReactiveUserDetailsService,
    authRepository: AuthRepository<User>,
    private val dashboardService: DashboardService,
    private val objectMapper: ObjectMapper
): AuthenticatedWebSocketHandler(jwtToken, reactiveUserDetailsService, authRepository) {

    // use Sink(thread-safe) way to programmatically push data into a Flux or Mono
    private val sink: Sinks.Many<String> = Sinks.many().multicast().onBackpressureBuffer()

    // use Dispatchers.Default to run coroutine in the background with CPU
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun handleAuthenticatedSession(
        session: WebSocketSession,
        userId: String,
    ): Mono<Void> {

        clientSession[userId] = session
        logger.info("New dashboard client connected: $userId")

        // Send initial dashboard data when the client connects
        scope.launch{
            try {
                val dashboardData = dashboardService.getDashboardData()
                val json = objectMapper.writeValueAsString(dashboardData)
                sink.tryEmitNext(json)
            } catch (e: Exception) {
                println("Error fetching initial dashboard data: ${e.message}")
            }
        }
        val input = session.receive()
            .map { it.payloadAsText }
            .doOnNext { message -> println("Received message from dashboard client: $message")
                // Optional: Handle specific client requests
                when (message) {
                    "refresh" -> refreshDashboard()
                    "ping" -> sink.tryEmitNext("""{"type":"pong"}""") }
            }
        val output = session.send( sink.asFlux().map(session::textMessage) )
        return output.and(input)
    }

    /** * Triggers a full dashboard data refresh and broadcasts to all connected clients */
    fun refreshDashboard() {
        scope.launch {
            try {
                val dashboardData = dashboardService.getDashboardData()
                val json = objectMapper.writeValueAsString(dashboardData)
                sink.tryEmitNext(json)
                println("Dashboard refreshed and broadcasted to all clients")
            } catch (e: Exception) {
                println("Error refreshing dashboard: ${e.message}")
            }
        }
    }

}