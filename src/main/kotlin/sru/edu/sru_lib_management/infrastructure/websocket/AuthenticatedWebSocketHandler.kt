package sru.edu.sru_lib_management.infrastructure.websocket

import io.jsonwebtoken.JwtException
import kotlinx.coroutines.reactor.mono
import okhttp3.internal.userAgent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import sru.edu.sru_lib_management.auth.domain.jwt.BearerToken
import sru.edu.sru_lib_management.auth.domain.jwt.JwtToken
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.auth.domain.repository.AuthRepository
import sru.edu.sru_lib_management.infrastructure.websocket.helper.ConnectedClient
import sru.edu.sru_lib_management.infrastructure.websocket.helper.WebSocketSessionRegistry
import java.util.concurrent.ConcurrentHashMap

abstract class AuthenticatedWebSocketHandler(
    private val jwtToken: JwtToken,
    private val reactiveUserDetailsService: ReactiveUserDetailsService,
    private val authRepository: AuthRepository<User>,
): WebSocketHandler {

    @Autowired
    private val sessionRegistry: WebSocketSessionRegistry = WebSocketSessionRegistry()

    protected val logger = LoggerFactory.getLogger(this::class.java)!!
    protected val clientSession = ConcurrentHashMap<String, WebSocketSession>()
    protected open fun getAllowedRoles(): List<String> = listOf(Role.SUPER_ADMIN.name, Role.ADMIN.name)

   //protected var role: Role? = null

    override fun getSubProtocols(): List<String> =
        listOf("Bearer", "bearer")

    override fun handle(session: WebSocketSession): Mono<Void> {

        return authenticateSession(session)
            .flatMap { authResult ->
                when (authResult) {
                    is SocketAuthResult.Success -> {
                        sessionRegistry.register(
                            authResult.userId,
                            authResult.role,
                            session
                        )
                        handleAuthenticatedSession(session, authResult.userId)
                            .doFinally {
                                sessionRegistry.unregister(authResult.userId)
                            }
                    }
                    is SocketAuthResult.Unauthorized -> {
                        logger.warn("Authorization denied: ${authResult.reason}")
                        // 1008 is the standard code for Policy Violation (RBAC/Auth failure)
                        session.close(CloseStatus(1008, authResult.reason))
                    }
                    is SocketAuthResult.Failure -> {
                        logger.warn("Authentication system failure: ${authResult.reason}")
                        // 1011 is Internal Error or use 4000+ for custom app errors
                        session.close(CloseStatus(1011, authResult.reason))
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
                //role = user.roles
                val email = user.email
                val userRole = user.roles.name
                logger.info("User roles: $userRole")

                if (userRole !in getAllowedRoles()) {
                    logger.warn("Access denied for user $userId with role $userRole")
                    return@flatMap Mono.just(SocketAuthResult.Unauthorized("Insufficient permissions"))
                }

                reactiveUserDetailsService.findByUsername(email)
                    .map { userDetails ->
                        if (jwtToken.isValidToken(bearerToken, userDetails))
                            SocketAuthResult.Success(userId, user.roles)
                        else {
                            logger.warn("Token validation failed for user: $userId")
                            SocketAuthResult.Unauthorized("Token validation failed")
                        }
                    }
            }
        }catch (e: JwtException) {
            logger.error("JWT validation error: ${e.message}")
            Mono.just(SocketAuthResult.Unauthorized("Invalid token."))
        } catch (e: Exception) {
            logger.error("Token validation error: ${e.message}", e)
            Mono.just(SocketAuthResult.Failure("Authentication failed"))
        }
    }

    protected abstract fun handleAuthenticatedSession(
        session: WebSocketSession, userId: String
    ): Mono<Void>

    protected sealed class SocketAuthResult {
        data class Success(
            val userId: String,
            val role: Role
        ) : SocketAuthResult()
        data class Failure(val reason: String) : SocketAuthResult()
        data class Unauthorized(val reason: String) : SocketAuthResult()
    }

    /*
    // use sec-websocket-protocols
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
    }*/

    // use query param
    fun extractToken(session: WebSocketSession): String? {
        return session.handshakeInfo.uri.query
            ?.let {
                UriComponentsBuilder
                    .fromUri(session.handshakeInfo.uri)
                    .build()
                    .queryParams
                    .getFirst("token")
            }
    }
}