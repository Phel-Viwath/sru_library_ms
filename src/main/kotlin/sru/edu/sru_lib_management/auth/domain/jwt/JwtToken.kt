/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.auth.domain.model.CustomUserDetails
import sru.edu.sru_lib_management.infrastructure.ApiKeyConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtToken @Autowired constructor(
    apiKeyConfig: ApiKeyConfig
) {
    private val logger = LoggerFactory.getLogger(JwtToken::class.java)

    private val key = apiKeyConfig.jwtToken.toByteArray()
    private val secretKey = Keys.hmacShaKeyFor(key)
    private val phaser = Jwts.parserBuilder().setSigningKey(secretKey).build()

    fun generateAccessToken(userId: String, roles: List<String>): BearerToken{
        return createToken(userId, 15, roles)
    }

    fun generateRefreshToken(userId: String, roles: List<String>): BearerToken{
        return createToken(userId, 43200, roles)
    }

    fun createToken(userId: String, minute: Long, roles: List<String>): BearerToken {
        val claim: Map<String, Any> = mapOf("roles" to roles)
        val token = Jwts.builder()
            .setClaims(claim)
            .setSubject(userId)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plus(minute, ChronoUnit.MINUTES)))
            .signWith(secretKey)
            .compact()
        return BearerToken(token)
    }

    fun extractUserId(token: BearerToken): String{
        return phaser
            .parseClaimsJws(token.value)
            .body.subject
    }

    fun isValidToken(token: BearerToken, userDetails: UserDetails?): Boolean{
        val claims = phaser.parseClaimsJws(token.value).body
        val unexpired = claims.expiration.after(Date.from(Instant.now()))
        val roles = claims["roles"] as List<*>?
        val userId = (userDetails as? CustomUserDetails)?.userId
        val userDetailsRole = userDetails?.authorities?.firstOrNull()?.authority

        val isSubjectCorrect = claims.subject == userId
        val isRoleCorrect = roles?.contains(userDetailsRole)

        return unexpired && isSubjectCorrect && isRoleCorrect == true
    }

    fun isRefreshToken(token: BearerToken): Boolean = try {
        val claims = phaser.parseClaimsJws(token.value).body
        val issueAt = claims.issuedAt.toInstant()
        val expiration = claims.expiration.toInstant()
        val duration = ChronoUnit.MINUTES.between(issueAt, expiration)
        duration == 43200L
    }catch (e: Exception){
        logger.error("${e.message}")
        false
    }


}
