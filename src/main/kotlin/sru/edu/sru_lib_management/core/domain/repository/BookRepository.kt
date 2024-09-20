/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto
import sru.edu.sru_lib_management.core.domain.dto.FundCount
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookIncome
import sru.edu.sru_lib_management.core.domain.model.Books
import sru.edu.sru_lib_management.core.domain.repository.crud.ICrudRepository
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

@Repository
interface BookRepository : ICrudRepository<Books, String>{
    suspend fun bookAvailable(): List<BookAvailableDto>
    suspend fun bookTrash(bookId: String, date: LocalDate): String
    suspend fun recoveryBook(bookId: String): String
    suspend fun emptyTrash(deleteDate: LocalDate)

    fun getAllBookInTrash(): Flow<Books>
    suspend fun alertTrashMessage(currentDate: LocalDate): List<Books>

    suspend fun universityFunding(): List<FundCount>
    suspend fun allBookDonation(): List<FundCount>
}