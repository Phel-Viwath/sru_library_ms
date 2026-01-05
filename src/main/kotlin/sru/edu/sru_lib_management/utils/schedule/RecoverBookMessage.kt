/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.utils.schedule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.core.domain.model.NotificationType
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.core.domain.service.NotificationService
import sru.edu.sru_lib_management.infrastructure.websocket.notification.BookTrashNotification
import sru.edu.sru_lib_management.infrastructure.websocket.notification.NotificationSocket
import sru.edu.sru_lib_management.infrastructure.websocket.notification.TrashBookDto
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate

@Component
class RecoverBookMessage (
    private val bookRepository: BookRepository,
    private val notificationSocket: NotificationSocket,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(RecoverBookMessage::class.java)
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Scheduled(cron = "0 44 22 * * ?", zone = "Asia/Phnom_Penh")
    fun alertRecoveryBook() {
        serviceScope.launch {
            val books = bookRepository.alertTrashMessage(indoChinaDate())

            if (books.isNotEmpty()) {
                val bookTitles = books.joinToString(", ") { it.bookTitle }

                notificationService.notifyRole(
                    role = Role.ADMIN,
                    type = NotificationType.BOOK_TRASH_ALERT,
                    title = "Books will be deleted soon",
                    message = "The following books will be deleted: $bookTitles",
                    referenceId = null
                )

                val payload = books.map {
                    TrashBookDto(
                        bookId = it.bookId,
                        title = it.bookTitle
                    )
                }

                val notification = BookTrashNotification(
                    title = "Books will be deleted soon",
                    books = payload
                )

                notificationSocket.notifyAdmins(notification)
            }
        }
    }

}