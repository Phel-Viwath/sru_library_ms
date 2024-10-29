/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.BorrowDto
import sru.edu.sru_lib_management.core.domain.service.BorrowService

@Component
class BorrowHandler (
    private val borrowService: BorrowService
){

    @PreAuthorize("hasRole('USER')")
    suspend fun countBorrowsPerWeek(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        when(val result = borrowService.countBorrowPerWeek()){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
        }
    }

    @PreAuthorize("hasRole('USER')")
    suspend fun saveBorrow(
        request: ServerRequest
    ): ServerResponse = coroutineScope {

        val borrowDto = request.bodyToMono<BorrowDto>().awaitFirst()

        when(val result = borrowService.saveBorrow(borrowDto)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
        }
    }

    @PreAuthorize("hasRole('USER')")
    suspend fun getAllBorrow(request: ServerRequest): ServerResponse = coroutineScope {
        val borrowData = borrowService.getBorrows()
        ServerResponse.ok().bodyAndAwait(borrowData)
    }

    @PreAuthorize("hasRole('USER')")
    suspend fun getAllOverDueBooks(request: ServerRequest): ServerResponse = coroutineScope {
        val allOverDue = borrowService.overDueService()
        ServerResponse.ok().bodyAndAwait(allOverDue)
    }

    @PreAuthorize("hasRole('USER')")
    suspend fun bookReturned(
        request: ServerRequest
    ): ServerResponse = coroutineScope {

        val studentId = request.queryParamOrNull("studentId")?.toLong()
        val bookId = request.queryParamOrNull("bookId")

        if (studentId == null || bookId == null)
            return@coroutineScope ServerResponse.badRequest().buildAndAwait()

        when(val result = async {borrowService.bookReturned(studentId, bookId)}.await()){
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
        }
    }

    @PreAuthorize("hasRole('USER')")
    suspend fun extendBorrowBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val id = request.queryParamOrNull("id")?.toLong()
            ?: return@coroutineScope ServerResponse.badRequest().buildAndAwait()

        when(val result = borrowService.extendBorrow(id)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.ok().bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.ok().bodyValueAndAwait(result.clientErrMsg)
        }
    }
}