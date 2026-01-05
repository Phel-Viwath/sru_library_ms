package sru.edu.sru_lib_management.infrastructure.websocket.notification

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.infrastructure.websocket.helper.WebSocketSessionRegistry

@Service
class NotificationSocket(
    private val sessionRegistry: WebSocketSessionRegistry,
    private val objectMapper: ObjectMapper
) {
    fun notifyAdmins(notification: BookTrashNotification) {
        val json = objectMapper.writeValueAsString(notification)

        sessionRegistry.sendToRoles(
            roles = setOf(Role.ADMIN, Role.SUPER_ADMIN),
            message = json
        )
    }
}

data class BookTrashNotification(
    val type: String = "BOOK_TRASH_ALERT",
    val title: String,
    val books: List<TrashBookDto>,
    val timestamp: Long = System.currentTimeMillis()
)
data class TrashBookDto(
    val bookId: String,
    val title: String
)
