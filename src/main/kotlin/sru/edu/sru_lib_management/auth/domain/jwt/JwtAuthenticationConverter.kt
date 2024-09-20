/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.jwt

import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange.request.headers.getFirst("Authorization"))
            .filter { request ->
                // get only request that start with Bearer
                request.startsWith("Bearer ")
            }
            .map { token ->
                //get the token by cut it from the request 7 character
                token.substring(7)
            }.map { tokenValue ->
                // Store token in BearerToken Class
                BearerToken(tokenValue)
            }
    }
}