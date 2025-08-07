/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.data.repository

import io.r2dbc.spi.Row
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.*
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.auth.domain.dto.UserDto
import sru.edu.sru_lib_management.auth.domain.model.Role
import sru.edu.sru_lib_management.auth.domain.model.User
import sru.edu.sru_lib_management.auth.domain.repository.AuthRepository

@Repository
class AuthRepositoryImp(
    private val client: DatabaseClient
) : AuthRepository<User> {

    override suspend fun save(entity: User) {
        client.sql(SAVE_USER_QUERY)
            .bindValues(paramMap(entity))
            .await()
    }

    override suspend fun update(entity: User): Boolean {
        return client.sql(UPDATE_USER_QUERY)
            .bindValues(paramMap(entity))
            .fetch()
            .awaitRowsUpdated() > 0
    }

    override suspend fun findByUserId(userId: String): User? {
        return client.sql(FIND_USER_BY_ID)
            .bind("userId", userId)
            .map { row: Row, _ -> row.userRowMapping()}
            .awaitSingleOrNull()
    }

    override suspend fun findByEmail(email: String): User? {
        return client.sql(FIND_USER_BY_USERNAME)
            .bind("email", email)
            .map { row: Row, _ -> row.userRowMapping()}
            .awaitSingleOrNull()
    }

    override suspend fun changeRole(email: String, role: Role): Boolean {
        return client.sql(CHANGE_ROLE)
            .bind("role", role)
            .bind("email", email)
            .fetch()
            .awaitRowsUpdated() > 0
    }

    override suspend fun getAll(): List<UserDto> {
        return client.sql(GET_ALL_USER)
            .map { row ->
                UserDto(
                    username = row.get("username", String::class.java)!!,
                    role = row.get("roles", Role::class.java)!!,
                    email = row.get("email", String::class.java)!!
                )
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun getRole(email: String): Role {
        return client.sql("SELECT roles from users WHERE username = :email")
            .bind("email", email)
            .map { row ->
                val role = row.get("roles", Role::class.java)!!
                role
            }
            .awaitSingle()
    }

    private fun paramMap(user: User): Map<String, Any> = mapOf(
        "userId" to user.userId,
        "email" to user.email,
        "username" to user.username,
        "password" to user.password,
        "roles" to user.roles
    )

    private fun Row.userRowMapping(): User{
        return User(
            userId = this.get("userId", String::class.java)!!,
            username = this.get("username", String::class.java)!!,
            email = this.get("email", String::class.java)!!,
            password = this.get("password", String::class.java)!!,
            roles = this.get("roles", Role::class.java)!!
        )
    }


    companion object{
        private const val SAVE_USER_QUERY = "Insert Into users(userId, email, username, password, roles) VALUES (:userId, :email, :username, :password, :roles);"
        private const val UPDATE_USER_QUERY = "Update users SET password = :password, roles = :roles, username = :username Where email = :email"
        private const val FIND_USER_BY_ID = "SELECT * FROM users WHERE userId = :userId"
        private const val FIND_USER_BY_USERNAME = "SELECT * from users WHERE email = :email"
        private const val CHANGE_ROLE = "UPDATE users set roles = :role WHERE email = :email"
        private const val GET_ALL_USER = "SELECT username, email, roles FROM users"
    }

}