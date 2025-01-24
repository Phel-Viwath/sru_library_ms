package sru.edu.sru_lib_management.infrastructure.websocket

import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationWebSocketHandler: NotificationWebSocketHandler
) {
    fun notifyAllClients(message: String) {
        notificationWebSocketHandler.sendToAllClient(message)
    }
}