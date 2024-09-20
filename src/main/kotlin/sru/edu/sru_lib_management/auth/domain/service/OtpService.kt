/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.service

import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.auth.domain.model.OtpDetails
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class OtpService(
    private val emailService: EmailService,
    private val hunterService: HunterService
) {
    private val otpMemory = ConcurrentHashMap <String, OtpDetails>()

    fun generateOtp(): String{
        val codeLength = 6
        val otp = (1..codeLength)
            .map { Random.nextInt(0, 10) }
            .joinToString("")
        return otp
    }

    suspend fun generateAndSent(email: String): String{
        val otp = generateOtp()
        otpMemory[email] = OtpDetails(otp, indoChinaDateTime().plusMinutes(5))
        val message = """
            <p>Hi $email,</p>

            <p>We received your request for a OTP code to use with your SRU Library account.</p>

            <p>Your OTP code is: <strong>$otp</strong></p>

            <p>If you didn't request this code, you can safely ignore this email. Someone else might have typed your email address by mistake.</p>

            <p>Thanks,<br>
            The SRU Library account team</p>
        """
        emailService.sendEmail(email, "Your OTP code", message)
        return otp
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