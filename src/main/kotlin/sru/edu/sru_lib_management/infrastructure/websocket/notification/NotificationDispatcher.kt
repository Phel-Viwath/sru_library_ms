package sru.edu.sru_lib_management.infrastructure.websocket.notification

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import sru.edu.sru_lib_management.auth.domain.model.Role
import java.util.concurrent.ConcurrentHashMap

@Component
class NotificationDispatcher {
    private val sessions =
        ConcurrentHashMap<String, WebSocketSession>()

    fun register(userId: String, session: WebSocketSession) {
        sessions[userId] = session
    }

    fun unregister(userId: String) {
        sessions.remove(userId)
    }

    fun sendToUser(userId: String, payload: String) {
        sessions[userId]?.send(
            Mono.just(
                sessions[userId]!!.textMessage(payload)
            )
        )?.subscribe()
    }

    fun sendToRole(
        role: Role,
        userRoles: Map<String, Role>,
        payload: String
    ) {
        userRoles
            .filter { it.value == role }
            .keys
            .forEach { sendToUser(it, payload) }
    }
}