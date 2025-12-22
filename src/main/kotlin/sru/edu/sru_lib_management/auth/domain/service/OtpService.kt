/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.auth.domain.model.OtpDetails
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class OtpService(
    private val emailService: EmailService,
    //private val hunterService: HunterService
) {
    private val otpMemory = ConcurrentHashMap <String, OtpDetails>()
    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    fun generateOtp(): String{
        val codeLength = 6
        val otp = (1..codeLength)
            .map { Random.nextInt(0, 10) }
            .joinToString("")
        return otp
    }

    suspend fun generateAndSent(email: String): String? {
        return try {
            val otpCode = generateOtp()
            otpMemory[email] = OtpDetails(otpCode, indoChinaDateTime().plusMinutes(3))
            val message = """
                <p>Hi $email,</p>
    
                <p>We received your request for a OTP code to use with your SRU Library account.</p>
    
                <p>Your OTP code is: <strong>$otpCode</strong></p>
    
                <p>If you didn't request this code, you can safely ignore this email. Someone else might have typed your email address by mistake.</p>
    
                <p>Thanks,<br>
                The SRU Library account team</p>
            """
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    emailService.sendEmail(email, "Your OTP code", message)
                } catch (e: Exception) {
                    logger.error("Failed to send email: ${e.message}")
                }
            }
            otpCode
        }catch (e: Exception){
            logger.error("${e.message}")
            null
        }
    }

    fun verifyOtp(otp: String, email: String): Boolean{
        val otpDetails = otpMemory[email]
        if (otpDetails != null && otpDetails.isValid() && otpDetails.otp == otp){
            otpMemory.remove(email)
            return true
        }
        return false
    }

}