/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.utils

import org.springframework.http.HttpStatus

object ResponseStatus {
    val BAD_REQUEST = HttpStatus.BAD_REQUEST
    val INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR
    val OK = HttpStatus.OK
    val CREATED = HttpStatus.CREATED
    val NOT_FOUND = HttpStatus.NOT_FOUND
    val CONFLICT = HttpStatus.CONFLICT
    val ACCEPTED = HttpStatus.ACCEPTED
}