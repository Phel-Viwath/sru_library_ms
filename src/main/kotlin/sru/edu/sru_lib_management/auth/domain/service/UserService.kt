package sru.edu.sru_lib_management.auth.domain.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import sru.edu.sru_lib_management.auth.domain.dto.UserDto
import sru.edu.sru_lib_management.auth.domain.dto.toUserDto
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.auth.domain.repository.AuthRepository
import sru.edu.sru_lib_management.common.CoreResult

@Service
class UserService (
    private val authRepository: AuthRepository<User>
){

    suspend fun getProfile(userId: String): CoreResult<UserDto> {
        return try {
            val user: User = authRepository.findByUserId(userId)
                ?: return CoreResult.ClientError("User not found")
            val userDto: UserDto = user.toUserDto()
            CoreResult.Success(userDto)
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }

    }

    suspend fun updateRole(role: Role, email: String): Boolean {
        return try {
            val roleUpdated = authRepository.changeRole(email, role)
            roleUpdated
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    suspend fun getAllUser(): List<UserDto>{
        try {
            return authRepository.getAll()
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

}