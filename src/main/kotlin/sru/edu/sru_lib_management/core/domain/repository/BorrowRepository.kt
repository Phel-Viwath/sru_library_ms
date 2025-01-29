/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.core.domain.dto.BorrowDetail
import sru.edu.sru_lib_management.core.domain.dto.CompareValue
import sru.edu.sru_lib_management.core.domain.dto.analytic.MostBorrow
import sru.edu.sru_lib_management.core.domain.model.Borrow
import sru.edu.sru_lib_management.core.domain.repository.crud.ICrudRepository
import java.sql.Date
import java.time.LocalDate

@Repository
interface BorrowRepository : ICrudRepository<Borrow, Long> {
    suspend fun countBorrowPerWeek(): Map<LocalDate, Int>
    suspend fun countCurrentAndPreviousBorrow(date: LocalDate, period: Int): CompareValue

    suspend fun extendBorrow(borrowId: Long): Long

    //
    fun findOverDueBook(): Flow<Borrow>
    suspend fun bookReturned(borrowId: Long): Boolean
    suspend fun findBorrowByStudentIdBookId(studentId: Long, bookId: String): List<Borrow>
    suspend fun getAllBorrowForEachMajor(startDate: LocalDate, endDate: LocalDate): Flow<Map<String, Int>>

    fun getMostBorrow(startDate: LocalDate, endDate: LocalDate): Flow<MostBorrow>
    suspend fun getNotBringBackByStudentId(studentId: Long): List<Borrow?>

    suspend fun getAllBorrowDetail(): List<BorrowDetail>
    suspend fun searchBorrow(keyword: String): List<BorrowDetail>

    fun getActiveBorrowDetail(): Flow<BorrowDetail>
}
