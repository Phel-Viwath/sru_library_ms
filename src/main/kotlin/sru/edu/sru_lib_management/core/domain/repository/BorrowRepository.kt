/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.core.domain.dto.CompareValue
import sru.edu.sru_lib_management.core.domain.dto.analytic.MostBorrow
import sru.edu.sru_lib_management.core.domain.model.BorrowBook
import sru.edu.sru_lib_management.core.domain.repository.crud.ICrudRepository
import java.sql.Date
import java.time.LocalDate

@Repository
interface BorrowRepository : ICrudRepository<BorrowBook, Long> {
    fun customBorrow(date: Date): Flow<BorrowBook>
    suspend fun countBorrowPerWeek(): Map<LocalDate, Int>
    suspend fun countCurrentAndPreviousBorrow(date: LocalDate, period: Int): CompareValue

    suspend fun extendBorrow(borrowId: Long): Long

    //
    fun findOverDueBook(): Flow<BorrowBook>
    suspend fun bookReturned(borrowId: Long): Boolean
    suspend fun findBorrowByStudentIdBookId(studentId: Long, bookId: String): List<BorrowBook>
    suspend fun getAllBorrowForEachMajor(startDate: LocalDate, endDate: LocalDate): Flow<Map<String, Int>>

    fun getMostBorrow(startDate: LocalDate, endDate: LocalDate): Flow<MostBorrow>
}
