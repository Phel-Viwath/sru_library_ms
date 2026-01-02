package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.core.domain.model.Notification
import sru.edu.sru_lib_management.core.domain.model.NotificationType

@Service
interface NotificationService {

    suspend fun notifyRole(
        role: Role,
        type: NotificationType,
        title: String,
        message: String,
        referenceId: String? = null
    )

    suspend fun notifyUser(
        userId: String,
        type: NotificationType,
        title: String,
        message: String,
        referenceId: String? = null
    )

    fun getUnreadForUser(
    userId: String,
    role: Role
    ): Flow<Notification>

    suspend fun markAsRead(notificationId: Long)
}