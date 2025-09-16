/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.infrastructure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration


@Configuration
class ApiKeyConfig(
    @param:Value("\${jwt.token}") val jwtToken: String,
    @param:Value("\${hunter.api.key}") val apiKey: String,
)