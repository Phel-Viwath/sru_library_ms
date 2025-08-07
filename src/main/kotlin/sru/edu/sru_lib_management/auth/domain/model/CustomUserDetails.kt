package sru.edu.sru_lib_management.auth.domain.model

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    val userId: String,
    private val email: String,
    private val password: String,
    private val role: String
): UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority("ROLE_$role"))
    override fun getPassword() = password
    override fun getUsername() = email // Still used by Spring
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}