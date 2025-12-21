/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.BorrowDetail
import sru.edu.sru_lib_management.core.domain.dto.BorrowDto
import sru.edu.sru_lib_management.core.domain.model.Borrow
import sru.edu.sru_lib_management.core.domain.service.BorrowService
import java.time.LocalDate

@Component
class BorrowHandler (
    private val borrowService: BorrowService
){

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun countBorrowsPerWeek(): ServerResponse = coroutineScope {
        when(val result: CoreResult<Map<LocalDate, Int>> = borrowService.countBorrowPerWeek()){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun saveBorrow(request: ServerRequest): ServerResponse = coroutineScope {

        val borrowDto: BorrowDto = request.bodyToMono<BorrowDto>().awaitFirst()

        when(val result = borrowService.saveBorrow(borrowDto)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getAllBorrow(): ServerResponse = coroutineScope {
        val borrowData: Flow<Borrow> = borrowService.getBorrows()
        ServerResponse.ok().bodyAndAwait(borrowData)
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getAllOverDueBooks(): ServerResponse = coroutineScope {
        val allOverDue: Flow<Borrow> = borrowService.overDueService()
        ServerResponse.ok().bodyAndAwait(allOverDue)
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun bookReturned(
        request: ServerRequest
    ): ServerResponse = coroutineScope {

        val studentId: Long? = request.queryParamOrNull("studentId")?.toLong()
        val bookId: String? = request.queryParamOrNull("bookId")

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

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun extendBorrowBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val id: Long = request.queryParamOrNull("id")?.toLong()
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

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun borrowDetails(): ServerResponse = coroutineScope {
        val result: List<BorrowDetail> = borrowService.getBorrowDetail()
        ServerResponse.ok().bodyValueAndAwait(result)
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun searchBorrow(request: ServerRequest): ServerResponse = coroutineScope {
        val keyword: String = request.queryParams()["keyword"]?.firstOrNull()
            ?: return@coroutineScope ServerResponse.badRequest().buildAndAwait()
        val result = borrowService.searchBorrow(keyword)
        ServerResponse.ok().bodyValueAndAwait(result)
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getActiveBorrow(): ServerResponse = coroutineScope{
        val data: Flow<BorrowDetail> = borrowService.getActiveBorrowed()
        ServerResponse.ok().bodyAndAwait(data)
    }

}