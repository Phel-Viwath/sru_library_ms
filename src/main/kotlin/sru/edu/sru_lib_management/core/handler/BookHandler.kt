/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.BookDto
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.utils.ResponseStatus.ACCEPTED
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR
import sru.edu.sru_lib_management.utils.ResponseStatus.OK
import sru.edu.sru_lib_management.utils.toBookDto
import java.time.YearMonth

@Component
class BookHandler(
    private val bookService: BookService,
) {

    private val logger = LoggerFactory.getLogger(BookHandler::class.java)
    /*
        * -> http://localhost:8090/api/v1/book
        * This Endpoint use to add book to database
        * */
    @PreAuthorize("hasRole('USER')")
    suspend fun addNewBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val books = request.bodyToMono<List<BookDto>>().awaitFirst()
        when(val result = bookService.saveBook(books)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
        }
    }

    /*
   * -> http://localhost:8090/api/v1/book
   * This Endpoint use to get all book from database
   * */

    @PreAuthorize("hasRole('USER')")
    suspend fun getBooks(request: ServerRequest): ServerResponse = coroutineScope {
        val allBooks = bookService.getAllBooks()
        ServerResponse.ok().bodyAndAwait(allBooks)
    }

    /*
   * -> http://localhost:8090/api/v1/book/current-book
   * This Endpoint use to get all book from database
   * */
    @PreAuthorize("hasRole('USER')")
    suspend fun currentAvailableBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val bookAvailable = bookService.currentAvailableBook()
        ServerResponse.ok().bodyAndAwait(bookAvailable)
    }

    /*
   * -> http://localhost:8090/api/v1/book
   * This Endpoint use to update book in database
   * */
    @PreAuthorize("hasRole('USER')")
    suspend fun updateBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope {

        val bookDto = request.bodyToMono<BookDto>().awaitFirst()

        when(val result = bookService.updateBook(bookDto)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
        }
    }

    /*
    * -> http://localhost:8090/api/v1/book/{id}
    * This Endpoint use to get book in database
    * */
    @PreAuthorize("hasRole('USER')")
    suspend fun getBookById(request: ServerRequest): ServerResponse{
        val bookId = request.pathVariable("bookId")
        return when(val result = bookService.getBook(bookId)){
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /*
    * -> http://localhost:8090/api/v1/book/{id}
    * This Endpoint use to update book in database
    * */
    @PreAuthorize("hasRole('USER')")
    suspend fun deleteBook(request: ServerRequest): ServerResponse{
        val bookId = request.pathVariable("bookId")
        return when(val result = bookService.deleteBook(bookId)){
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Success ->
                ServerResponse.status(OK).bodyValueAndAwait(result.data)
        }
    }


    /*
    * -> http://localhost:8090/api/v1/book/available
    * This Endpoint use to get available book
    * */
    @PreAuthorize("hasRole('USER')")
    suspend fun availableBook(request: ServerRequest): ServerResponse = coroutineScope {
        when(val result = bookService.getAvailableBook()){
            is CoreResult.Success ->
                ServerResponse.status(OK).bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /////////// Trash http://localhost:8090/api/v1/book/trash
    @PreAuthorize("hasRole('USER')")
    suspend fun moveToTrash(
        request: ServerRequest
    ): ServerResponse = coroutineScope{

        val bookId = request.queryParams()["bookId"]
            ?: return@coroutineScope ServerResponse.noContent().buildAndAwait()

        when(val result = bookService.moveToTrash(bookId)){
            is CoreResult.Success ->
                ServerResponse.status(ACCEPTED).bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    // http://localhost:8090/api/v1/book/recover
    @PreAuthorize("hasRole('USER')")
    suspend fun recoverBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val bookId = request.queryParams()["bookId"]
            ?: return@coroutineScope ServerResponse.noContent().buildAndAwait()

        when(val result = bookService.recoveryBooks(bookId)){
            is CoreResult.Success ->
                ServerResponse.status(ACCEPTED).bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    // http://localhost:8090/api/v1/book/in-trash
    @PreAuthorize("hasRole('USER')")
    suspend fun getBooksInTrash(request: ServerRequest): ServerResponse = coroutineScope{
        val bookInTrash =  bookService.getBooksInTrash().map {
            it.toBookDto()
        }
        ServerResponse.ok().bodyAndAwait(bookInTrash)
    }

    @PreAuthorize("hasRole('USER')")
    suspend fun getBookIncome(request: ServerRequest): ServerResponse{
        val sYearMonth = request.queryParam("sYearMonth")
            .map { YearMonth.parse(it) }
            .orElse(null)
        val eYearMonth = request.queryParam("eYearMonth")
            .map { YearMonth.parse(it) }
            .orElse(null)
        val bookIncomes = bookService.getBookIncome(sYearMonth, eYearMonth).asFlow()
        return ServerResponse.ok().bodyAndAwait(bookIncomes)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun aboutBookData(
        request: ServerRequest
    ): ServerResponse {
        val data =  GlobalScope.async { bookService.aboutBookData()}.await()
        return ServerResponse.ok().json().bodyValueAndAwait(data)
    }

}