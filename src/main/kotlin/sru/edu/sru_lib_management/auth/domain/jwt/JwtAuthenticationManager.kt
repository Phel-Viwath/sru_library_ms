/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.jwt

import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import sru.edu.sru_lib_management.auth.data.repository.AuthRepositoryImp
import sru.edu.sru_lib_management.auth.domain.model.CustomUserDetails
import sru.edu.sru_lib_management.common.InvalidBearerToken

@Component
class JwtAuthenticationManager(
    private val users: ReactiveUserDetailsService,
    private val jwtToken: JwtToken,
    private val authRepository: AuthRepositoryImp
) : ReactiveAuthenticationManager {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationManager::class.java)

    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        return Mono.justOrEmpty(authentication)
            .filter {
                it is BearerToken
            }
            .cast(BearerToken::class.java)
            .flatMap { token ->
                mono { validate(token) }
            }
            .onErrorMap { error ->
                InvalidBearerToken(error.message)
            }
    }

    private suspend fun validate(token: BearerToken): Authentication{
        val userId = jwtToken.extractUserId(token)
        val user = authRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("User ID not found.")
        // Create CustomUserDetails manually
        val userDetails = CustomUserDetails(
            userId = user.userId,
            email = user.email,
            password = user.password,
            role = user.roles.name
        )

        if (jwtToken.isValidToken(token, userDetails)){
            return UsernamePasswordAuthenticationToken(
                userDetails.username,
                userDetails.password,
                userDetails.authorities
            )
        }
        throw IllegalArgumentException("Token is not valid.")
    }
}