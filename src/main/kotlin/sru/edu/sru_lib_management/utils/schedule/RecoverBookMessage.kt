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
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.infrastructure.websocket.notification.NotificationSocket
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate

@Component
class RecoverBookMessage (
    private val bookRepository: BookRepository,
    private val notificationSocket: NotificationSocket
) {
    private val logger = LoggerFactory.getLogger(RecoverBookMessage::class.java)

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutableMapBook: MutableMap<String, String> = mutableMapOf()

    @Scheduled(cron = "0 00 00 * * ?", zone = "Asia/Phnom_Penh")
    fun alertRecoveryBook(){
        serviceScope.launch {
            val books = bookRepository.alertTrashMessage(indoChinaDate())
            books.forEach { book ->
                mutableMapBook[book.bookId] = book.bookTitle
            }
            logger.info("Websocket message: $mutableMapBook")
            if (mutableMapBook.isNotEmpty()){
                sentRecoverNotification(mutableMapBook)
            }
        }
    }

    private fun sentRecoverNotification(bookMap: Map<String, String>){
        val bookId = bookMap.keys
        val bookTitle = bookMap.values
        val msg = "Book will delete soon: $bookId: $bookTitle"
        notificationSocket.notifyAllClients(msg)
    }



}