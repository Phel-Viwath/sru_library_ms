/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto
import sru.edu.sru_lib_management.core.domain.dto.BookDto
import sru.edu.sru_lib_management.core.domain.model.Books
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.utils.ResponseStatus.ACCEPTED
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR
import sru.edu.sru_lib_management.utils.ResponseStatus.OK
import sru.edu.sru_lib_management.utils.toBookDto
import java.io.File
import java.io.InputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Component
class BookHandler(
    private val bookService: BookService,
) {

    //private val logger = LoggerFactory.getLogger(BookHandler::class.java)
    /*
        * -> http://localhost:8090/api/v1/book
        * This Endpoint use to add book to database
        * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun searchBook(request: ServerRequest): ServerResponse = coroutineScope{
        val keyword: String = request.queryParams()["keyword"]?.firstOrNull()
            ?: return@coroutineScope ServerResponse.badRequest().buildAndAwait()
        val books = bookService.searchBooks(keyword)
        ServerResponse.ok().bodyAndAwait(books)
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun addNewBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val books: List<BookDto> = request.bodyToMono<List<BookDto>>().awaitFirst()
        when(val result = bookService.saveBook(books)){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun uploadBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val multipartData: MultiValueMap<String, Part> = request.awaitMultipartData()
        println("Multipart keys received: ${multipartData.keys}")

        val filePart = multipartData["book_file"]?.firstOrNull() as? FilePart
        if (filePart == null) {
            println("FilePart is null or invalid.")
            return@coroutineScope ServerResponse.badRequest().bodyValueAndAwait("File is missing or invalid.")
        }

        //Save the file to a temporary location or process it directly
        val tempFile = File.createTempFile("upload", ".xlsx")
        filePart.transferTo(tempFile).awaitFirstOrNull()
        //val tempFile = File.createTempFile("upload", ".xlsx")
        val inputStream = tempFile.inputStream()
        val book = parseExcelFile(inputStream)
        tempFile.delete()

        when(val result = bookService.saveBook(book.asFlow().toList())){
            is CoreResult.Success ->
                ServerResponse.ok().bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(500).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.badRequest().bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /*
   * -> http://localhost:8090/api/v1/book
   * This Endpoint use to get all book from database
   * */

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getBooks(): ServerResponse = coroutineScope {
        val allBooks = bookService.getAllBooks()
        ServerResponse.ok().bodyAndAwait(allBooks)
    }

    /*
   * -> http://localhost:8090/api/v1/book/current-book
   * This Endpoint use to get all book from database
   * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun currentAvailableBook(): ServerResponse = coroutineScope{
        val bookAvailable: Flow<Books> = bookService.currentAvailableBook().asFlow()
        ServerResponse.ok().bodyAndAwait(bookAvailable)
    }

    /*
   * -> http://localhost:8090/api/v1/book
   * This Endpoint use to update book in database
   * */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun updateBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope {

        val bookDto: BookDto = request.bodyToMono<BookDto>().awaitFirst()

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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getBookById(request: ServerRequest): ServerResponse{
        val bookId: String = request.pathVariable("bookId")
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
    * This Endpoint use to update book in Database
    * */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    suspend fun deleteBook(request: ServerRequest): ServerResponse{
        val bookId: String = request.pathVariable("bookId")
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
    * This Endpoint use to get the Available book
    * */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun availableBook(): ServerResponse = coroutineScope {
        when(val result: CoreResult<List<BookAvailableDto>> = bookService.getAvailableBook()){
            is CoreResult.Success ->
                ServerResponse.status(OK).bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    /////////// Trash http://localhost:8090/api/v1/book/trash
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun moveToTrash(
        request: ServerRequest
    ): ServerResponse = coroutineScope{

        val bookId: MutableList<String> = request.queryParams()["bookId"]
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun recoverBook(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val bookId: MutableList<String> = request.queryParams()["bookId"]
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getBooksInTrash(): ServerResponse = coroutineScope{
        val bookInTrash: Flow<BookDto> =  bookService.getBooksInTrash().map {
            it.toBookDto()
        }
        ServerResponse.ok().bodyAndAwait(bookInTrash)
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun getBookIncome(request: ServerRequest): ServerResponse{
        val sYearMonth: YearMonth = request.queryParam("sYearMonth")
            .map { YearMonth.parse(it) }
            .orElse(null)  ?: return ServerResponse.badRequest().buildAndAwait()

        val eYearMonth = request.queryParam("eYearMonth")
            .map { YearMonth.parse(it) }
            .orElse(null) ?: return ServerResponse.badRequest().buildAndAwait()

        val bookIncomes = bookService.getBookIncome(sYearMonth, eYearMonth).asFlow()
        return ServerResponse.ok().bodyAndAwait(bookIncomes)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun aboutBookData(): ServerResponse {
        val data: Map<String, Int> =  GlobalScope.async { bookService.aboutBookData()}.await()
        return ServerResponse.ok().json().bodyValueAndAwait(data)
    }

    private fun parseExcelFile(inputStream: InputStream): List<BookDto> {
        val workBook = WorkbookFactory.create(inputStream)
        val sheet = workBook.getSheetAt(0)
        val books = mutableListOf<BookDto>()

        for (row in sheet.drop(1)){
            val bookId = row.getCell(0).stringValue()
            val bookTitle = row.getCell(1).stringValue()
            val bookQuan = row.getCell(2).numericValue()?.toInt() ?: 0
            val languageId = row.getCell(3).stringValue()
            val collegeId = row.getCell(4).stringValue()
            val author = row.getCell(5)?.stringValue() ?: ""
            val publicationYear = row.getCell(6)?.numericValue()?.toInt() ?: 0
            val genre = row.getCell(7).stringValue()
            val receiveDate = row.getCell(8)?.dateValue()

            books.add(
                BookDto(
                    bookId = bookId,
                    bookTitle = bookTitle,
                    bookQuan = bookQuan,
                    languageId = languageId,
                    collegeId = collegeId,
                    author = author,
                    publicationYear = publicationYear,
                    genre = genre,
                    receiveDate = receiveDate
                )
            )
        }
        workBook.close()
        return books
    }
    // Extension functions to handle cell types dynamically
    private fun Cell.stringValue(): String {
        return when (cellType) {
            CellType.STRING -> stringCellValue
            CellType.NUMERIC -> numericCellValue.toInt().toString()
            else -> ""
        }
    }

    private fun Cell.numericValue(): Double? {
        return when (cellType) {
            CellType.NUMERIC -> numericCellValue
            CellType.STRING -> stringCellValue.toDoubleOrNull()
            else -> null
        }
    }
    private fun Cell.dateValue(): LocalDate? {
        return if (cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(this)) {
            DateUtil.getJavaDate(numericCellValue).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            stringValue().let { LocalDate.parse(it) }
        }
    }

}