/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.common

sealed class CoreResult<out T> {
    data class Success<out T>(val data: T): CoreResult<T>()
    data class Failure(val errorMsg: String): CoreResult<Nothing>()
    data class ClientError(val clientErrMsg: String): CoreResult<Nothing>()
}