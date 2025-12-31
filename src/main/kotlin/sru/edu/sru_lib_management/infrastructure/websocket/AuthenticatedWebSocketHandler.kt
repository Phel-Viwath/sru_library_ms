package sru.edu.sru_lib_management.infrastructure.websocket

import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import sru.edu.sru_lib_management.auth.domain.jwt.BearerToken
import sru.edu.sru_lib_management.auth.domain.jwt.JwtToken
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.auth.domain.repository.AuthRepository
import java.util.concurrent.ConcurrentHashMap

abstract class AuthenticatedWebSocketHandler(
    private val jwtToken: JwtToken,
    private val reactiveUserDetailsService: ReactiveUserDetailsService,
    private val authRepository: AuthRepository<User>,
): WebSocketHandler {

    protected val logger = LoggerFactory.getLogger(this::class.java)!!
    protected val clientSession = ConcurrentHashMap<String, WebSocketSession>()

    protected open fun getAllowedRoles(): List<String> = listOf(Role.SUPER_ADMIN.name, Role.ADMIN.name)

    override fun getSubProtocols(): List<String> =
        listOf("Bearer", "bearer")

    override fun handle(session: WebSocketSession): Mono<Void> {

        return authenticateSession(session)
            .flatMap { authResult ->
                when (authResult) {
                    is SocketAuthResult.Success -> handleAuthenticatedSession(session, authResult.userId)
                    is SocketAuthResult.Failure -> {
                        logger.warn("Authentication failed: ${authResult.reason}")
                        session.close(CloseStatus.NOT_ACCEPTABLE.withReason(authResult.reason))
                    }
                }
            }
    }

    private fun authenticateSession(
        session: WebSocketSession
    ): Mono<SocketAuthResult>{

        val tokenString = extractToken(session)
            ?: return Mono.just(SocketAuthResult.Failure("No token provided"))

        return try {
            val bearerToken = BearerToken(tokenString)
            val userId = jwtToken.extractUserId(bearerToken)
            val userMono: Mono<User> = mono { authRepository.findByUserId(userId) }

            userMono.flatMap { user ->
                val email = user.email
                val userRole = user.roles.name
                logger.info("User roles: $userRole")

                if (userRole !in getAllowedRoles()) {
                    logger.warn("Access denied for user $userId with role $userRole")
                    return@flatMap Mono.just(SocketAuthResult.Failure("Insufficient permissions"))
                }

                reactiveUserDetailsService.findByUsername(email)
                    .map { userDetails ->
                        if (jwtToken.isValidToken(bearerToken, userDetails))
                            SocketAuthResult.Success(userId)
                        else {
                            logger.warn("Token validation failed for user: $userId")
                            SocketAuthResult.Failure("Token validation failed")
                        }
                    }
            }
        }catch (e: io.jsonwebtoken.JwtException) {
            logger.error("JWT validation error: ${e.message}")
            Mono.just(SocketAuthResult.Failure("Invalid token: ${e.message}"))
        } catch (e: Exception) {
            logger.error("Token validation error: ${e.message}", e)
            Mono.just(SocketAuthResult.Failure("Authentication failed"))
        }
    }

    protected abstract fun handleAuthenticatedSession(
        session: WebSocketSession, userId: String
    ): Mono<Void>

    protected sealed class SocketAuthResult {
        data class Success(val userId: String) : SocketAuthResult()
        data class Failure(val reason: String) : SocketAuthResult()
    }

    private fun extractToken(session: WebSocketSession): String? {
        val header = session.handshakeInfo.headers
            .getFirst("Sec-WebSocket-Protocol")
            ?: return null

        // Case 1: "Bearer <token>"
        if (header.startsWith("Bearer ", ignoreCase = true)) {
            return header.removePrefix("Bearer ").trim()
        }

        // Case 2: "auth-token, <token>"
        val parts = header.split(",").map { it.trim() }
        if (parts.size >= 2 && parts[0].equals("auth-token", ignoreCase = true)) {
            return parts[1]
        }

        return null
    }
}