/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto
import sru.edu.sru_lib_management.core.domain.dto.BookDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookEachCollege
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookIncome
import sru.edu.sru_lib_management.core.domain.dto.analytic.TotalBook
import sru.edu.sru_lib_management.core.domain.model.Books
import java.time.LocalDate
import java.time.YearMonth

@Service
interface BookService {
    fun getAllBooks(): Flow<Books>
    suspend fun saveBook(books: List<BookDto>): CoreResult<List<BookDto>>
    suspend fun updateBook(books: BookDto): CoreResult<BookDto>
    suspend fun getBook(bookId: String): CoreResult<BookDto>
    suspend fun deleteBook(bookId: String): CoreResult<Boolean>
    suspend fun getAvailableBook(): CoreResult<List<BookAvailableDto>>
    fun currentAvailableBook(): Flow<Books>

    /// Trash
    suspend fun moveToTrash(bookId: List<String>): CoreResult<String>

    fun getBooksInTrash(): Flow<Books>
    suspend fun emptyTrash()
    suspend fun recoveryBooks(bookId: List<String>): CoreResult<String>

    suspend fun bookLanguage(): TotalBook

    suspend fun getBookDataForEachCollege(startDate: YearMonth?, endDate: YearMonth?): List<BookEachCollege>

    suspend fun getBookIncome(startMonth: YearMonth?, endMonth: YearMonth?): List<BookIncome>

    suspend fun aboutBookData(): Map<String, Int>
}
