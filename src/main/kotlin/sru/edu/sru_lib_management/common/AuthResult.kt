/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.common

sealed class AuthResult<out T> {
    data class Success <out T> (val data: T): AuthResult<T>()
    data class Failure(val errorMsg: String): AuthResult<Nothing>()
    data class InputError(val inputErrMsg: String): AuthResult<Nothing>()
}