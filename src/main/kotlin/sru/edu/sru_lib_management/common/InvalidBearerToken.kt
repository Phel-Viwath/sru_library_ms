/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.common

import org.springframework.security.core.AuthenticationException

class InvalidBearerToken(message: String?): AuthenticationException(message)