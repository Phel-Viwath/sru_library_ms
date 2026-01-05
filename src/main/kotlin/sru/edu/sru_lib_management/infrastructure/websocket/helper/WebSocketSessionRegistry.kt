package sru.edu.sru_lib_management.infrastructure.websocket.helper

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import sru.edu.sru_lib_management.auth.domain.model.Role
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionRegistry {
    private val clients = ConcurrentHashMap<String, ConnectedClient>()

    fun register(
        userId: String,
        role: Role,
        session: WebSocketSession
    ) {
        clients[userId] = ConnectedClient(userId, role, session)
    }

    fun unregister(userId: String) {
        clients.remove(userId)
    }

    fun getByRole(role: Role): List<ConnectedClient> =
        clients.values.filter { it.role == role }

    fun getByUserId(userId: String): ConnectedClient? =
        clients[userId]

    fun sendToRoles(roles: Set<Role>, message: String) {
        clients.values
            .filter { it.role in roles && it.session.isOpen }
            .forEach {
                it.session.send(
                    Mono.just(it.session.textMessage(message))
                ).subscribe()
            }
    }
}