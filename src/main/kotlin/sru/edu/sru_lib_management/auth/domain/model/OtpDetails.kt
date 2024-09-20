/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.model

import java.time.LocalDateTime

data class OtpDetails(val otp: String, val expiredTime: LocalDateTime){
    fun isValid() = LocalDateTime.now().isBefore(expiredTime)
}
