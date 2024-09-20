/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.service

import jakarta.mail.internet.InternetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {
    @Value("\${spring.mail.username}") private val email: String? = null
    suspend fun sendEmail(to: String, subject: String, text: String){
        withContext(Dispatchers.IO){
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            message.setFrom(InternetAddress("SRU_LIBRARY"))
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(text, true)
            mailSender.send(message)
            println("Email has sent")
        }
    }
}