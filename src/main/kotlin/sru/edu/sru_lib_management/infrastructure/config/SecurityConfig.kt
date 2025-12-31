/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.infrastructure.config

import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.web.cors.CorsConfiguration
import reactor.core.publisher.Mono
import sru.edu.sru_lib_management.auth.data.repository.AuthRepositoryImp
import sru.edu.sru_lib_management.auth.domain.jwt.JwtAuthenticationConverter
import sru.edu.sru_lib_management.auth.domain.jwt.JwtAuthenticationManager
import sru.edu.sru_lib_management.auth.domain.model.CustomUserDetails
import java.util.*

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig (
    private val repository: AuthRepositoryImp,
    @param:Value("\${spring.mail.password}") val mailSenderPassword: String,
    @param:Value("\${spring.mail.username}") val mailSenderUsername: String,
) {

    private val dashboardAccessRole = listOf("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
    private val notificationAccessRole = listOf("ROLE_SUPER_ADMIN", "ROLE_ADMIN")

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailServices(): ReactiveUserDetailsService = ReactiveUserDetailsService { email ->
        mono {
            val users = repository.findByEmail(email)
            users?.let { user ->
                CustomUserDetails(
                    userId = user.userId,
                    email = user.email,
                    password = user.password,
                    role = user.roles.name
                )
            }
        }
    }

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        converter: JwtAuthenticationConverter,
        authManager: JwtAuthenticationManager
    ): SecurityWebFilterChain {
        val filter = AuthenticationWebFilter(authManager)
        filter.setServerAuthenticationConverter(converter)
        return http
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { exchange, _ ->
                    Mono.fromRunnable {
                        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                        exchange.response.headers.remove(HttpHeaders.WWW_AUTHENTICATE)
                    }
                }
            }
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers("/notifications", "/dashboard").permitAll()
                    .anyExchange().authenticated()
            }
            .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .csrf { it.disable() }
            .cors { corsSpec ->
                corsSpec.configurationSource {
                    CorsConfiguration().apply {
                        allowedOrigins = listOf(
                            "http://localhost:5173",
                            "http://localhost:5174",
                            "http://localhost:5175",
                            "https://react-js-inky-three.vercel.app",
                            "https://react-js-inky-three.vercel.app/"
                        )
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
                        allowCredentials = true
                        addAllowedHeader("*")
                    }
                }
            }
            .build()
    }

    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = "smtp.gmail.com"
        mailSender.port = 587
        mailSender.username = mailSenderUsername
        mailSender.password = mailSenderPassword

        val props: Properties = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.debug"] = "true"

        return mailSender
    }

    private fun checkSocketAccess(
        authentication: Mono<Authentication>,
        roles: List<String>
    ): Mono<AuthorizationDecision>{
        return authentication
            .map { auth ->
                val hasRole = auth.authorities.any { authority ->
                    authority.authority in roles
                }
                AuthorizationDecision(hasRole)
            }
            .defaultIfEmpty(AuthorizationDecision(false))
    }

}
