package sru.edu.sru_lib_management.infrastructure.websocket.notification

import org.springframework.stereotype.Service

@Service
class NotificationSocket(
    private val notificationWebSocketHandler: NotificationWebSocketHandler
) {
    fun notifyAllClients(message: String) {
        notificationWebSocketHandler.sendToAllClient(message)
    }
}