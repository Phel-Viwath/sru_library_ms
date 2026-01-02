package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.core.domain.model.Notification
import sru.edu.sru_lib_management.core.domain.model.NotificationStatus

@Repository
interface NotificationRepository : CoroutineCrudRepository<Notification, Long> {

    fun findByTargetRoleAndIsReadFalse(
        role: Role
    ): Flow<Notification>

    fun findByTargetUserIdAndIsReadFalse(
        userId: String
    ): Flow<Notification>

    @Modifying
    @Query("""
        UPDATE notifications
        SET is_read = true
        WHERE notification_id = :id
    """)
    suspend fun markAsRead(id: Long)

    @Modifying
    @Query("""
        UPDATE notifications
        SET status = :status
        WHERE notification_id = :id
    """)
    suspend fun updateStatus(id: Long, status: NotificationStatus)
}