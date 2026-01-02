package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.core.domain.model.Notification
import sru.edu.sru_lib_management.core.domain.model.NotificationType
import sru.edu.sru_lib_management.core.domain.repository.NotificationRepository
import sru.edu.sru_lib_management.core.domain.service.NotificationService
import sru.edu.sru_lib_management.infrastructure.websocket.notification.NotificationDispatcher

@Component
class NotificationServiceImp (
    private val notificationRepository: NotificationRepository,
    private val dispatcher: NotificationDispatcher
) : NotificationService {

    override suspend fun notifyRole(
        role: Role,
        type: NotificationType,
        title: String,
        message: String,
        referenceId: String?,
    ) {
        val notification = Notification(
            type = type,
            title = title,
            message = message,
            targetRole = role
        )

        notificationRepository.save(notification)
        dispatcher.sendToRole(role, mapOf("admin" to Role.ADMIN), message)
    }

    override suspend fun notifyUser(
        userId: String,
        type: NotificationType,
        title: String,
        message: String,
        referenceId: String?,
    ) {
        TODO("Not yet implemented")
    }

    override fun getUnreadForUser(
        userId: String,
        role: Role,
    ): Flow<Notification> {
        TODO("Not yet implemented")
    }

    override suspend fun markAsRead(notificationId: Long) {
        TODO("Not yet implemented")
    }
}