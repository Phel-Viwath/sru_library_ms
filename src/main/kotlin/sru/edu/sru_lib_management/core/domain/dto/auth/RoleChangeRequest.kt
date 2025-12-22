package sru.edu.sru_lib_management.core.domain.dto.auth

data class RoleChangeRequest(
    val email: String,
    val role: String
)