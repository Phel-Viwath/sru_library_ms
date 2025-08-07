/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import sru.edu.sru_lib_management.auth.data.repository.AuthRepositoryImp
import sru.edu.sru_lib_management.auth.domain.dto.LoginRequest
import sru.edu.sru_lib_management.auth.domain.dto.RegisterRequest
import sru.edu.sru_lib_management.auth.domain.dto.UserDto
import sru.edu.sru_lib_management.auth.domain.jwt.BearerToken
import sru.edu.sru_lib_management.auth.domain.jwt.JwtToken
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.common.AuthResult
import java.util.*


@Service
class AuthService(
    private val userDetailsService: ReactiveUserDetailsService,
    private val authRepository: AuthRepositoryImp,
    private val jwtSupport: JwtToken,
    private val encoder: PasswordEncoder
){
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    suspend fun register(request: RegisterRequest): AuthResult<String> {
        return runCatching {

            val encryptPassword = encoder.encode(request.password)
            val newUser = User(
                userId = UUID.randomUUID().toString(),
                username = request.username,
                email = request.email,
                password = encryptPassword,
                roles = Role.USER
            )
            authRepository.save(newUser)
            "Success!"
        }.fold(
            onSuccess = {
                AuthResult.Success(it)
            },
            onFailure = {
                AuthResult.Failure("${it.message}")
                throw Exception("${it.message}")
            }
        )
    }

    suspend fun login(request: LoginRequest): AuthResult<Map<String, String>> {
        return try {
            /////// Check field of auth request
            if (request.email.isBlank() || request.password.isBlank())
                AuthResult.InputError("Please enter username and password.")

            // find user with their email
            val userDetails = userDetailsService.findByUsername(request.email).awaitSingleOrNull()
                ?: return AuthResult.InputError("Invalid username or password.")

            // check password
            if (encoder.matches(request.password, userDetails.password)) {
                val userId = authRepository.findByEmail(request.email)?.userId ?: "Unknown USER"
                authSuccess(userId, userDetails)
            } else {
                AuthResult.InputError("Invalid password")
            }

        } catch (e: Exception){
            AuthResult.Failure("${e.printStackTrace()}")
        }
    }

    suspend fun refreshToken(refreshToken: String): AuthResult<Map<String, String>>{
        return try {
            val bearerToken = BearerToken(refreshToken)
            val userId = jwtSupport.extractUserId(bearerToken)
            val user = authRepository.findByUserId(userId)
                ?: return AuthResult.InputError("Invalid refresh token.")
            val userDetails = userDetailsService.findByUsername(user.username).awaitSingleOrNull()
                ?: return AuthResult.InputError("Invalid refresh token.")

            if (jwtSupport.isValidToken(bearerToken, userDetails) && jwtSupport.isRefreshToken(bearerToken)){
                authSuccess(userId, userDetails)
            }else{
                AuthResult.InputError("Invalid refresh token")
            }
        }catch (e: Exception){
            e.printStackTrace()
            AuthResult.Failure(e.message.toString())
        }
    }

    suspend fun updatePassword(email: String, password: String): AuthResult<String>{
        return try {
            var role: Role = Role.USER
            var username = ""
            var userId = ""

            // find user in database
            authRepository.findByEmail(email)?.let { user ->
                role = user.roles
                username = user.username
                userId = user.userId
            } ?: return AuthResult.InputError("Invalid email.")
            // encrypt password
            val encryptPassword = encoder.encode(password)
            val user = User(userId = userId, email = email, username = username, password = encryptPassword, roles = role)
            val updated = authRepository.update(user)
            if (updated)
                AuthResult.Success("Success.")
            else
                AuthResult.InputError("Fail.")
        }catch (e: Exception){
            AuthResult.Failure(e.message.toString())
        }
    }

    suspend fun existEmail(email: String): Boolean{
        return try {
            val user = userDetailsService.findByUsername(email).awaitSingleOrNull()
            user != null
        }catch (e: Exception){
            logger.error("${e.message}")
            false
        }
    }

    suspend fun updateRole(role: Role, email: String): Boolean {
        return try {
            val roleUpdated = authRepository.changeRole(email, role)
            roleUpdated
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
    suspend fun getAllUser(): List<UserDto>{
        try {
            return authRepository.getAll()
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    private fun authSuccess(userId: String, userDetails: UserDetails): AuthResult.Success<Map<String, String>>{
        val roles = userDetails.authorities.map { grantedAuthority -> grantedAuthority.authority }
        val newAccessToken = jwtSupport.generateAccessToken(userId, roles).value
        val newRefreshToken = jwtSupport.generateRefreshToken(userId, roles).value
         return AuthResult.Success(
            mapOf(
                "accessToken" to newAccessToken,
                "refreshToken" to newRefreshToken,
                "role" to roles.toString(),
                "userId" to userId
            )
        )
    }

}
