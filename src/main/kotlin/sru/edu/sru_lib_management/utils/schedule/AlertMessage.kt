/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.utils.schedule

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.core.domain.dto.BookDto
import sru.edu.sru_lib_management.core.domain.service.MessageService

@Component
class AlertMessage @Autowired constructor(
    private val messageService: MessageService
) {

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Phnom_Penh")
    @Scheduled(cron = "0 0 15 * * *", zone = "Asia/Phnom_Penh")
    fun bookTrash(){
        runBlocking {
            messageService.alertRecoveryBook()
        }
    }
}