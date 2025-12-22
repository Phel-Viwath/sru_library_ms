/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.auth.domain.dto.AuthResponse
import sru.edu.sru_lib_management.auth.domain.dto.LoginRequest
import sru.edu.sru_lib_management.auth.domain.dto.RefreshTokenRequest
import sru.edu.sru_lib_management.auth.domain.dto.RegisterRequest
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.service.AuthService
import sru.edu.sru_lib_management.auth.domain.service.HunterService
import sru.edu.sru_lib_management.auth.domain.service.OtpService
import sru.edu.sru_lib_management.common.AuthResult
import sru.edu.sru_lib_management.core.domain.dto.auth.RequestOtp
import sru.edu.sru_lib_management.core.domain.dto.auth.RequestOtpVerify
import sru.edu.sru_lib_management.core.domain.dto.auth.RoleChangeRequest
import sru.edu.sru_lib_management.utils.ResponseStatus.ACCEPTED
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.CREATED
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR
import sru.edu.sru_lib_management.utils.ResponseStatus.OK
import java.util.*

@Component
@Slf4j
class AuthHandler(
    private val service: AuthService,
    private val hunterService: HunterService,
    private val otpService: OtpService,
) {

    private val logger:Logger = LoggerFactory.getLogger(AuthHandler::class.java)

    suspend fun register(
        request: ServerRequest
    ): ServerResponse {
        val registerRequest: RegisterRequest = request.bodyToMono<RegisterRequest>().awaitSingle()
        val areFieldBlank =
            registerRequest.email.isBlank() || registerRequest.password.isBlank() || registerRequest.username.isBlank()
        val isPasswordTooShort = registerRequest.password.length < 8
        // check field
        if (areFieldBlank || isPasswordTooShort)
            return ServerResponse.badRequest().bodyValueAndAwait("Field cannot be blank, password must be greater than 8.")

        val validEmail = hunterService.verifyEmail(registerRequest.email)
        if (validEmail == "undeliverable")
            return ServerResponse.badRequest().bodyValueAndAwait("Invalid email")

        // check Username already exists or not
        val alreadyInUse = withContext(Dispatchers.IO) {
            service.existEmail(registerRequest.email)
        }
        if (alreadyInUse)
            return ServerResponse.status(HttpStatus.CONFLICT).bodyValueAndAwait("User already exist")

        return coroutineScope {
            when (val result = service.register(registerRequest)) {
                is AuthResult.Success ->
                    ServerResponse.status(CREATED).bodyValueAndAwait(result.data)
                is AuthResult.InputError ->
                    ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.inputErrMsg)
                is AuthResult.Failure ->
                    ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValue(result.errorMsg).awaitSingle()
            }
        }
    }

    suspend fun login(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val loginRequest: LoginRequest = request.bodyToMono<LoginRequest>().awaitSingle()
        val alreadyInUse = service.existEmail(loginRequest.email)
        if (!alreadyInUse)
             return@coroutineScope ServerResponse.badRequest().bodyValueAndAwait(AuthResponse(message = "Incorrect email."))
        when ( val result = service.login(loginRequest)) {
            is AuthResult.Success -> {
                val token = result.data
               ServerResponse.status(OK).bodyValueAndAwait(AuthResponse(
                   accessToken = token["accessToken"],
                   refreshToken = token["refreshToken"],
                   role = token["role"].toString().trim('[', ']').removePrefix("ROLE_"),
                   userId = token["userId"]
               ))
            }
            is AuthResult.InputError ->
                ServerResponse.badRequest().bodyValueAndAwait(AuthResponse(message = result.inputErrMsg))
            is AuthResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(AuthResponse(message = result.errorMsg))
        }
    }

    suspend fun refresh(
        request: ServerRequest
    ): ServerResponse {
        val tokenRequest: RefreshTokenRequest = request.bodyToMono<RefreshTokenRequest>().awaitSingle()
        return when (val result = service.refreshToken(tokenRequest.refreshToken)) {
            is AuthResult.Success -> {
                val token = result.data
                ServerResponse.ok().bodyValueAndAwait(
                    AuthResponse(
                        accessToken = token["accessToken"],
                        refreshToken = token["refreshToken"],
                        role = token["role"].toString().trim('[', ']').removePrefix("ROLE_"),
                        userId = token["userId"]
                    )
                )
            }
            is AuthResult.InputError ->
                ServerResponse.badRequest().bodyValueAndAwait(AuthResponse(message = result.inputErrMsg))
            is AuthResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(AuthResponse(message = result.errorMsg))
        }
    }

    suspend fun requestOtpCode(
        request: ServerRequest
    ): ServerResponse{
        val email: String = request.bodyToMono<RequestOtp>().awaitSingle().email
        if (email.isBlank()) return ServerResponse.badRequest().bodyValueAndAwait("Invalid username")

        val alreadyInUse = service.existEmail(email)
        if (!alreadyInUse) return ServerResponse.badRequest().bodyValueAndAwait("Incorrect username.")

        val processOtp = otpService.generateAndSent(email)
        return if (processOtp?.toIntOrNull() == null)
            ServerResponse.badRequest().bodyValueAndAwait("OTP could not be generated. Please try again later.")
        else
            ServerResponse.ok().bodyValueAndAwait("OTP Sent Successfully")

    }

    suspend fun verifyOtp(
        request: ServerRequest
    ): ServerResponse {
        val verifyRequest: RequestOtpVerify = request.bodyToMono<RequestOtpVerify>().awaitSingle()
        val otp: String = verifyRequest.otp
        val email: String = verifyRequest.email

        return when {
            otp.isBlank() -> ServerResponse.badRequest().bodyValueAndAwait("Invalid OTP")
            email.isBlank() -> ServerResponse.badRequest().bodyValueAndAwait("Invalid Email")
            otpService.verifyOtp(otp, email) -> ServerResponse.ok().bodyValueAndAwait("OTP Verified Successfully")
            else -> ServerResponse.badRequest().bodyValueAndAwait("Invalid or Expired OTP")
        }
    }

    suspend fun changePassword(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val loginRequest: LoginRequest = request.bodyToMono<LoginRequest>().awaitSingle()
        if (loginRequest.password.length < 8){
            return@coroutineScope ServerResponse.badRequest().bodyValueAndAwait("Password is too short.")
        }
        when(val result = service.updatePassword(loginRequest.email, loginRequest.password)){
            is AuthResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is AuthResult.InputError -> {
                logger.info(result.inputErrMsg)
                ServerResponse.badRequest().bodyValueAndAwait(result.inputErrMsg)
            }
            is AuthResult.Failure ->
                ServerResponse.badRequest().bodyValueAndAwait(result.errorMsg)
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    suspend fun changeRole(
        request: ServerRequest
    ): ServerResponse{

        val roleChangeRequest = request.bodyToMono<RoleChangeRequest>().awaitSingle()

        val email: String = roleChangeRequest.email
        val role = roleChangeRequest.role

        if (email.isBlank() || role.isBlank())
            return ServerResponse.badRequest().bodyValueAndAwait("Invalid email or role parameter")

        val roles = try {
            Role.valueOf(role.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            logger.info("${e.message}")
            return ServerResponse.badRequest().bodyValueAndAwait("Invalid role parameter")
        }
        logger.info("$roles")
        val roleUpdated = service.updateRole(roles, email)
        logger.info("$roleUpdated")
        return if (!roleUpdated)
            ServerResponse.status(BAD_REQUEST).buildAndAwait()
        else
            ServerResponse.status(ACCEPTED).bodyValueAndAwait("Role has changed.")

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    suspend fun getAllUser(): ServerResponse{
        val allUser =  service.getAllUser().asFlow()
        return ServerResponse.status(OK).bodyAndAwait(allUser)
    }

}
