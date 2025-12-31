/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.auth.domain.dto.LoginRequest
import sru.edu.sru_lib_management.auth.domain.dto.RegisterRequest
import sru.edu.sru_lib_management.auth.domain.jwt.BearerToken
import sru.edu.sru_lib_management.auth.domain.jwt.JwtToken
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.auth.domain.repository.AuthRepository
import sru.edu.sru_lib_management.common.AuthResult
import java.util.*


@Service
class AuthService(
    private val userDetailsService: ReactiveUserDetailsService,
    private val authRepository: AuthRepository<User>,
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

            // find Users with their email
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
                ?: return AuthResult.InputError("Invalid refresh token. Credential not found.")
            val userDetails = userDetailsService.findByUsername(user.email).awaitSingleOrNull()
                ?: return AuthResult.InputError("Invalid refresh token. User details not found")
            logger.info("Refresh token user details is ${userDetails.username}")

            if (jwtSupport.isValidToken(bearerToken, userDetails) && jwtSupport.isRefreshToken(bearerToken)){
                authSuccess(userId, userDetails)
            }else{
                AuthResult.InputError("Invalid refresh token")
            }
        }catch (e: Exception){
            logger.error("${e.message}")
            AuthResult.Failure(e.message.toString())
        }
    }

    suspend fun updatePassword(email: String, password: String): AuthResult<String>{
        return try {
            val existingUser = authRepository.findByEmail(email) ?: return AuthResult.InputError("Invalid email.")
            // encrypt password
            val encryptPassword = encoder.encode(password)
            val userUpdate = User(
                userId = existingUser.userId,
                email = email,
                username = existingUser.username,
                password = encryptPassword,
                roles = existingUser.roles
            )
            val updated = authRepository.update(userUpdate)
            if (updated)
                AuthResult.Success("Success.")
            else
                AuthResult.InputError("Fail.")
        }catch (e: Exception){
            e.printStackTrace()
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
