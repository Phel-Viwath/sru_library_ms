package sru.edu.sru_lib_management.infrastructure.websocket.notification

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.infrastructure.websocket.helper.WebSocketSessionRegistry

@Component
class NotificationDispatcher(
    private val sessionRegistry: WebSocketSessionRegistry
) {
    fun sendToUser(userId: String, payload: String) {
        sessionRegistry.getByUserId(userId)
            ?.session
            ?.send(Mono.just(
                sessionRegistry.getByUserId(userId)!!
                    .session.textMessage(payload)
            ))?.subscribe()
    }

    fun sendToRole(role: Role, payload: String) {
        sessionRegistry.getByRole(role)
            .forEach { client ->
                client.session.send(
                    Mono.just(
                        client.session.textMessage(payload)
                    )
                ).subscribe()
            }
    }
}