/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.utils.schedule

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.infrastructure.websocket.NotificationService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate

@Component
class RecoverBookMessage (
    private val bookRepository: BookRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(RecoverBookMessage::class.java)
    private val mutableMapBook: MutableMap<String, String> = mutableMapOf()

    @Scheduled(cron = "0 46 21 * * ?", zone = "Asia/Phnom_Penh")
    suspend fun alertRecoveryBook(){
        val books = bookRepository.alertTrashMessage(indoChinaDate())
        books.forEach { book ->
            mutableMapBook[book.bookId] = book.bookTitle
        }
        logger.info("Websocket message: $mutableMapBook")
//        if (mutableMapBook.isNotEmpty()){
//            sentRecoverNotification(mutableMapBook)
//        }
        sentRecoverNotification(mutableMapBook)
    }

    private fun sentRecoverNotification(bookMap: Map<String, String>){
        val bookId = bookMap.keys
        val bookTitle = bookMap.values
        val msg = "Book will delete soon: $bookId: $bookTitle"
        notificationService.notifyAllClients(msg)
    }



}