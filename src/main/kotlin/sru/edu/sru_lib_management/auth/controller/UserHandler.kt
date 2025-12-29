package sru.edu.sru_lib_management.auth.controller

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.auth.domain.dto.UserDto
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.service.UserService
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.auth.RoleChangeRequest
import sru.edu.sru_lib_management.utils.ResponseStatus.ACCEPTED
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.OK
import java.util.*

@Component
class UserHandler(
    private val userService: UserService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    suspend fun changeRole(
        request: ServerRequest
    ): ServerResponse{

        val roleChangeRequest = request.bodyToMono<RoleChangeRequest>().awaitSingle()

        val email: String = roleChangeRequest.email
        val role = roleChangeRequest.role

        if (email.isBlank() || role.isBlank())
            return ServerResponse.badRequest().bodyValueAndAwait("Invalid email or role parameter")

        val roles = try {
            Role.valueOf(role.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            logger.info("${e.message}")
            return ServerResponse.badRequest().bodyValueAndAwait("Invalid role parameter")
        }
        logger.info("$roles")
        val roleUpdated = userService.updateRole(roles, email)
        logger.info("$roleUpdated")
        return if (!roleUpdated)
            ServerResponse.status(BAD_REQUEST).buildAndAwait()
        else
            ServerResponse.status(ACCEPTED).bodyValueAndAwait("Role has changed.")

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    suspend fun getAllUser(): ServerResponse{
        val allUser =  userService.getAllUser().asFlow()
        return ServerResponse.status(OK).bodyAndAwait(allUser)
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    suspend fun getUserProfile(
        request: ServerRequest
    ): ServerResponse {
        val userId = request.pathVariable("userId")
        if (userId.isBlank())
            return ServerResponse.badRequest().bodyValueAndAwait("Invalid USER ID")
        return when(val result: CoreResult<UserDto> = userService.getProfile(userId)){
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
        }
    }
}