package sru.edu.sru_lib_management.infrastructure.route.core_route

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.auth.controller.AuthHandler

@Configuration
class AuthRouteConfig {

    @Bean
    fun authRoute(authHandler: AuthHandler): RouterFunction<ServerResponse> {
        return coRouter {
            accept(APPLICATION_JSON).nest {
                "/api/v1/auth".nest {
                    /**
                     * Registers a new user account.
                     * Requires [sru.edu.sru_lib_management.auth.domain.dto.RegisterRequest] with email, username, and password (min 8 chars).
                     * Validates email using [sru.edu.sru_lib_management.auth.domain.service.HunterService] and checks for duplicates.
                     * Returns [sru.edu.sru_lib_management.auth.domain.dto.AuthResponse] on a success or error message.
                     * @see AuthHandler.register
                     * @see sru.edu.sru_lib_management.auth.domain.dto.RegisterRequest
                     */
                    POST("/register", authHandler::register)

                    /**
                     * Authenticates a user and issues JWT tokens.
                     * Requires [sru.edu.sru_lib_management.auth.domain.dto.LoginRequest] with email and password.
                     * Returns [sru.edu.sru_lib_management.auth.domain.dto.AuthResponse] with accessToken, refreshToken, role, and userId.
                     * @see AuthHandler.login
                     * @see sru.edu.sru_lib_management.auth.domain.dto.LoginRequest
                     * @see sru.edu.sru_lib_management.auth.domain.dto.AuthResponse
                     */
                    POST("/login", authHandler::login)

                    /**
                     * Refreshes an expired access token.
                     * Requires request body [sru.edu.sru_lib_management.auth.domain.dto.RefreshTokenRequest] with valid refreshToken.
                     * Returns new [sru.edu.sru_lib_management.auth.domain.dto.AuthResponse] with updated tokens.
                     * @see AuthHandler.refresh
                     * @see sru.edu.sru_lib_management.auth.domain.dto.RefreshTokenRequest
                     */
                    POST("/refresh-token", authHandler::refresh)

                    /**
                     * Generates and sends OTP to the user's email for password reset.
                     * Request Body: [sru.edu.sru_lib_management.core.domain.dto.auth.RequestOtp]email (required).
                     * Validates email using [sru.edu.sru_lib_management.auth.domain.service.HunterService]
                     * and sends OTP via [sru.edu.sru_lib_management.auth.domain.service.OtpService].
                     * Returns success message or error.
                     * @see AuthHandler.requestOtpCode
                     * @see sru.edu.sru_lib_management.auth.domain.service.OtpService.generateAndSent
                     */
                    POST("/otp", authHandler::requestOtpCode)

                    /**
                     * Verifies the OTP code sent to the user's email.
                     * Request Body: [sru.edu.sru_lib_management.core.domain.dto.auth.RequestOtpVerify] otp (required), email (required).
                     * Returns "OTP Verified Successfully" or "Invalid or Expired OTP".
                     * @see AuthHandler.verifyOtp
                     * @see sru.edu.sru_lib_management.auth.domain.service.OtpService.verifyOtp
                     */
                    POST("/verify", authHandler::verifyOtp)

                    /**
                     * Changes user password (used after OTP verification).
                     * Requires [sru.edu.sru_lib_management.auth.domain.dto.LoginRequest] with email and new password (min 8 chars).
                     * Updates password in Database using [sru.edu.sru_lib_management.auth.domain.service.AuthService.updatePassword].
                     * @see AuthHandler.changePassword
                     * @see sru.edu.sru_lib_management.auth.domain.dto.LoginRequest
                     */
                    PUT("/change-password", authHandler::changePassword)

                    /**
                     * Updates user role (admin function).
                     * Query params: email (required), role (required - must be valid [sru.edu.sru_lib_management.auth.domain.model.Role] enum).
                     * Valid roles: ADMIN, SUPER_ADMIN.
                     * Returns "Role has changed." on success.
                     * @see AuthHandler.changeRole
                     * @see sru.edu.sru_lib_management.auth.domain.model.Role
                     * @see sru.edu.sru_lib_management.auth.domain.service.AuthService.updateRole
                     */
                    PUT("/change-role", authHandler::changeRole)

                    /**
                     * Gets all registered users.
                     * Returns [kotlinx.coroutines.flow.Flow] of user data.
                     * Admin/monitoring function.
                     * @see AuthHandler.getAllUser
                     * @see sru.edu.sru_lib_management.auth.domain.service.AuthService.getAllUser
                     */
                    GET("/users") { authHandler.getAllUser() }
                }
            }
        }
    }
}