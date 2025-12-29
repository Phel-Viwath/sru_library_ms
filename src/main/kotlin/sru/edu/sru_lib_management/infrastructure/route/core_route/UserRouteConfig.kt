package sru.edu.sru_lib_management.infrastructure.route.core_route

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.auth.controller.UserHandler

@Configuration
class UserRouteConfig {

    @Bean
    fun userRouter(userHandler: UserHandler) = coRouter{
        ((accept(APPLICATION_JSON) and "/api/v1/user")).nest {

            /**
             * Gets all registered users.
             * Returns [kotlinx.coroutines.flow.Flow] of user data.
             * Admin/monitoring function.
             * @see UserHandler.getAllUser
             * @see sru.edu.sru_lib_management.auth.domain.service.UserService.getAllUser
             */
            GET("") { userHandler.getAllUser() }

            /**
             * Gets user profile.
             * Returns [sru.edu.sru_lib_management.auth.domain.dto.UserDto].
             * @see UserHandler.getUserProfile
             * @see sru.edu.sru_lib_management.auth.domain.service.UserService.getProfile
             */
            GET("/profile/{userId}", userHandler::getUserProfile)

            /**
             * Updates user role (admin function).
             * Query params: email (required), role (required - must be valid [sru.edu.sru_lib_management.auth.domain.model.Role] enum).
             * Valid roles: ADMIN, SUPER_ADMIN.
             * Returns "Role has changed." on success.
             * @see UserHandler.changeRole
             * @see sru.edu.sru_lib_management.auth.domain.model.Role
             * @see sru.edu.sru_lib_management.auth.domain.service.UserService.updateRole
             */
            PUT("/change-role", userHandler::changeRole)

        }
    }
}