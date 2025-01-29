/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.dto.BorrowDetail
import sru.edu.sru_lib_management.core.domain.dto.BorrowDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.BorrowReturn
import sru.edu.sru_lib_management.core.domain.dto.analytic.MajorAttendBorrowed
import sru.edu.sru_lib_management.core.domain.dto.analytic.MostBorrow
import sru.edu.sru_lib_management.core.domain.model.Borrow
import java.time.LocalDate

@Service
interface BorrowService {
    // CRUD
    suspend fun saveBorrow(borrowDto: BorrowDto): CoreResult<Borrow>
    suspend fun updateBorrow(borrow: Borrow): CoreResult<Borrow>
    suspend fun getBorrow(borrowID: Long): CoreResult<Borrow?>
    fun getBorrows(): Flow<Borrow>
    suspend fun deleteBorrow(borrowID: Long): CoreResult<Boolean>

    //
    suspend fun countBorrowPerWeek(): CoreResult<Map<LocalDate, Int>>
    suspend fun analyticBorrow(date: LocalDate, period: Int): CoreResult<Analyze>

    // Check
    suspend fun bookReturned(studentId: Long, bookId: String): CoreResult<Boolean>
    suspend fun findBorrowByStudentIdBookId(studentId: Long, bookId: String): List<Borrow>
    fun overDueService(): Flow<Borrow>

    suspend fun extendBorrow(borrowId: Long): CoreResult<Long>

    suspend fun getBorrowDataEachMajor(startDate: LocalDate, endDate: LocalDate): List<MajorAttendBorrowed>

    fun mostBorrow(startDate: LocalDate, endDate: LocalDate): Flow<MostBorrow>

    suspend fun getBorrowAndReturn(startDate: LocalDate?, endDate: LocalDate?): List<BorrowReturn>
    suspend fun getBorrowDetail(): List<BorrowDetail>
    suspend fun searchBorrow(keyword: String): List<BorrowDetail>
    fun getActiveBorrowed(): Flow<BorrowDetail>
}
