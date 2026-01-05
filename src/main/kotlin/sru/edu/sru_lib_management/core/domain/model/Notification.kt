package sru.edu.sru_lib_management.core.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import sru.edu.sru_lib_management.auth.domain.model.Role
import java.time.LocalDateTime

@Table("notifications")
data class Notification(
    @Id
    val notificationId: Long? = null,

    val type: NotificationType,
    val title: String,
    val message: String,

    val targetRole: Role? = null,
    val targetUserId: String? = null,
    val referenceId: String? = null,
    val status: NotificationStatus? = null,

    val isRead: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
){
    init {
        require(targetRole != Role.USER){
            "Notifications can only be targeted to ADMIN or SUPER_ADMIN"
        }
    }
}

enum class NotificationType {
    BOOK_TRASH_ALERT,
    BOOK_APPROVAL_REQUEST,
    CHAT
}

enum class NotificationStatus {
    PENDING,
    APPROVED,
    REJECTED
}