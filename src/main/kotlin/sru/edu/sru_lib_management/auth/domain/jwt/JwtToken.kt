/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.infrastructure.ApiKeyConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtToken @Autowired constructor(
    apiKeyConfig: ApiKeyConfig
) {
    private val key = apiKeyConfig.jwtToken.toByteArray()
    private val secretKey = Keys.hmacShaKeyFor(key)
    private val phaser = Jwts.parserBuilder().setSigningKey(secretKey).build()

    fun generateAccessToken(email: String, roles: List<String>): BearerToken{
        return createToken(email, 15, roles)
    }

    fun generateRefreshToken(email: String, roles: List<String>): BearerToken{
        return createToken(email, 43200, roles)
    }

    fun createToken(email: String, minute: Long, roles: List<String>): BearerToken {
        val claim: Map<String, Any> = mapOf("roles" to roles)
        val token = Jwts.builder()
            .setClaims(claim)
            .setSubject(email)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plus(minute, ChronoUnit.MINUTES)))
            .signWith(secretKey)
            .compact()
        return BearerToken(token)
    }

    fun extractEmail(token: BearerToken): String{
        return phaser
            .parseClaimsJws(token.value)
            .body.subject
    }

    fun isValidToken(token: BearerToken, user: UserDetails?): Boolean{
        requireNotNull(user) { "User details cannot be null" }
        requireNotNull(token.value) { "Token value cannot be null" }
        val claims = phaser.parseClaimsJws(token.value).body
        val unexpired = claims.expiration.after(Date.from(Instant.now()))
        val roles = claims["roles"] as List<*>?
        return unexpired && (claims.subject == user.username) && roles?.contains(user.authorities?.first()?.authority) == true
    }

    fun isRefreshToken(token: BearerToken): Boolean = try {
        val claims = phaser.parseClaimsJws(token.value).body
        val issueAt = claims.issuedAt.toInstant()
        val expiration = claims.expiration.toInstant()
        val duration = ChronoUnit.MINUTES.between(issueAt, expiration)
        duration == 43200L
    }catch (e: Exception){
        e.printStackTrace()
        false
    }


}
