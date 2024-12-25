/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.service

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
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


@Service
class AuthService(
    private val userDetailsService: ReactiveUserDetailsService,
    private val repository: AuthRepositoryImp,
    private val jwtSupport: JwtToken,
    private val encoder: PasswordEncoder
){
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    suspend fun register(request: RegisterRequest): AuthResult<String> {
        return runCatching {

            val encryptPassword = encoder.encode(request.password)
            val newUser = User(
                username = request.username,
                email = request.email,
                password = encryptPassword,
                roles = Role.USER
            )
            repository.save(newUser)
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
            val user = userDetailsService.findByUsername(request.email).awaitSingleOrNull()
                ?: return AuthResult.InputError("Invalid username or password.")

            // check password
            if (encoder.matches(request.password, user.password)) {
                val username = repository.findByEmail(request.email)?.username ?: "Unknown USER"
                logger.info("$username ${user.username}")
                val roles = user.authorities.map { it.authority }
                val accessToken = jwtSupport.generateAccessToken(request.email, roles).value
                val refreshToken = jwtSupport.generateRefreshToken(request.email, roles).value
                AuthResult.Success(
                    mapOf(
                        "accessToken" to accessToken,
                        "refreshToken" to refreshToken,
                        "role" to roles.toString(),
                        "username" to username
                    )
                )
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
            val email = jwtSupport.extractEmail(bearerToken)
            val user = userDetailsService.findByUsername(email).awaitSingleOrNull()
                ?: return AuthResult.InputError("Invalid refresh token.")

            if (jwtSupport.isValidToken(bearerToken, user) && jwtSupport.isRefreshToken(bearerToken)){
                val roles = user.authorities.map { it.authority }
                val username = repository.findByEmail(email)?.username ?: "Unknown USER"
                val newAccessToken = jwtSupport.generateAccessToken(email, roles).value
                val newRefreshToken = jwtSupport.generateRefreshToken(email, roles).value
                AuthResult.Success(
                    mapOf(
                        "accessToken" to newAccessToken,
                        "refreshToken" to newRefreshToken,
                        "role" to roles.toString(),
                        "username" to username
                    )
                )
            }else{
                AuthResult.InputError("Invalid refresh token")
            }
        }catch (e: Exception){
            AuthResult.Failure(e.message.toString())
        }
    }

    suspend fun updatePassword(email: String, password: String): AuthResult<String>{
        return try {
            var role: Role = Role.USER
            var username = ""

            // find user in database
            repository.findByEmail(email)?.let { user ->
                role = user.roles
                username = user.username
            } ?: return AuthResult.InputError("Invalid email.")
            // encrypt password
            val encryptPassword = encoder.encode(password)
            val user = User(email = email, username = username, password = encryptPassword, roles = role)
            val updated = repository.update(user)
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
            false
        }
    }

    suspend fun updateRole(role: Role, email: String): Boolean {
        return try {
            val roleUpdated = repository.changeRole(email, role)
            roleUpdated
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
    suspend fun getAllUser(): List<UserDto>{
        try {
            return repository.getAll()
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

}
