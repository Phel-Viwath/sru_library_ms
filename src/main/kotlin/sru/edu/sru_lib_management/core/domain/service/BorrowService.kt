/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.dto.BorrowDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.BorrowReturn
import sru.edu.sru_lib_management.core.domain.dto.analytic.MajorAttendBorrowed
import sru.edu.sru_lib_management.core.domain.dto.analytic.MostBorrow
import sru.edu.sru_lib_management.core.domain.model.BorrowBook
import java.time.LocalDate

@Service
interface BorrowService {
    // CRUD
    suspend fun saveBorrow(borrowDto: BorrowDto): CoreResult<BorrowBook>
    suspend fun updateBorrow(borrowBook: BorrowBook): CoreResult<BorrowBook>
    suspend fun getBorrow(borrowID: Long): CoreResult<BorrowBook?>
    fun getBorrows(): Flow<BorrowBook>
    suspend fun deleteBorrow(borrowID: Long): CoreResult<Boolean>

    //
    suspend fun countBorrowPerWeek(): CoreResult<Map<LocalDate, Int>>
    suspend fun analyticBorrow(date: LocalDate, period: Int): CoreResult<Analyze>

    // Check
    suspend fun bookReturned(studentId: Long, bookId: String): CoreResult<Boolean>
    suspend fun findBorrowByStudentIdBookId(studentId: Long, bookId: String): List<BorrowBook>
    fun overDueService(): Flow<BorrowBook>

    suspend fun extendBorrow(borrowId: Long): CoreResult<Long>

    suspend fun getBorrowDataEachMajor(startDate: LocalDate, endDate: LocalDate): List<MajorAttendBorrowed>

    fun mostBorrow(startDate: LocalDate, endDate: LocalDate): Flow<MostBorrow>

    suspend fun getBorrowAndReturn(startDate: LocalDate?, endDate: LocalDate?): List<BorrowReturn>
    suspend fun getNotBringBackByStudentId(studentId: Long): CoreResult<String>

}
