package sru.edu.sru_lib_management.core.domain.dto.auth

data class RequestOtpVerify(
    val otp: String,
    val email: String
)