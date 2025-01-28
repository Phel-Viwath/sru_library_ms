/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import sru.edu.sru_lib_management.common.APIException
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.dto.BorrowDetail
import sru.edu.sru_lib_management.core.domain.dto.BorrowDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.BorrowReturn
import sru.edu.sru_lib_management.core.domain.dto.analytic.MajorAttendBorrowed
import sru.edu.sru_lib_management.core.domain.dto.analytic.MostBorrow
import sru.edu.sru_lib_management.core.domain.model.Borrow
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.core.domain.repository.BorrowRepository
import sru.edu.sru_lib_management.core.domain.repository.StudentRepository
import sru.edu.sru_lib_management.core.domain.service.BorrowService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate
import java.time.LocalDate
import java.time.YearMonth

@Component
class BorrowServiceImp(
    private val borrowRepository: BorrowRepository,
    private val bookRepository: BookRepository,
    private val studentRepository: StudentRepository
) : BorrowService {

    private val logger = LoggerFactory.getLogger(BorrowServiceImp::class.java)

    override suspend fun saveBorrow(
        borrowDto: BorrowDto
    ): CoreResult<Borrow> = runCatching {
        if (borrowDto.bookId.isBlank() || borrowDto.bookQuan <= 0 || borrowDto.studentId <= 0){
            return CoreResult.ClientError("Field cannot be blank.")
        }
        val book = bookRepository.getById(borrowDto.bookId)
            ?: return CoreResult.ClientError("Not found book with this ID: ${borrowDto.bookId}.")
        if(book.bookQuan < borrowDto.bookQuan)
            return CoreResult.ClientError("Please check book quan!.")
        studentRepository.getById(borrowDto.studentId)
            ?: return CoreResult.ClientError("Not found student with this ID: ${borrowDto.studentId}.")
        val borrowed = borrowRepository.getNotBringBackByStudentId(borrowDto.studentId)
        if (borrowed.size >= 2)
            return CoreResult.ClientError("You can not borrow more than two book in two weeks.")
        if (borrowed.size == 1 && borrowDto.bookQuan > 1)
            return CoreResult.ClientError("You can borrow only one book.")
        val borrow = Borrow(
            borrowId = null,
            bookId = borrowDto.bookId,
            bookQuan = borrowDto.bookQuan,
            studentId = borrowDto.studentId,
            borrowDate = indoChinaDate(),
            giveBackDate = indoChinaDate().plusWeeks(2),
            isBringBack = false,
            isExtend = false
        )
        borrowRepository.save(borrow)
    }.fold(
        onSuccess = {data ->
            CoreResult.Success(data)
        },
        onFailure = {e ->
            println(e.printStackTrace())
            CoreResult.Failure(e.message.toString())
        }
    )

    override suspend fun updateBorrow(
        borrow: Borrow
    ): CoreResult<Borrow> = runCatching{
        if (borrow.borrowId == null)
            return CoreResult.ClientError("Please enter id for update")
        borrowRepository.getById(borrow.borrowId) ?: return CoreResult.ClientError("Not found borrowing with this ID: ${borrow.borrowId}")
        borrowRepository.update(borrow)
    }.fold(
        onSuccess = {data ->
            CoreResult.Success(data)
        },
        onFailure = {
            println(it.printStackTrace())
            CoreResult.Failure(it.message.toString())
        }
    )

    override suspend fun getBorrow(
        borrowID: Long
    ): CoreResult<Borrow?> = runCatching {
        borrowRepository.getById(borrowID) ?: return CoreResult.ClientError("Not found borrowing with this ID: $borrowID")
    }.fold(
        onSuccess = {
            CoreResult.Success(it)
        },
        onFailure = {
            println(it.printStackTrace())
            CoreResult.Failure(it.message.toString())
        }
    )

    override fun getBorrows():Flow<Borrow> = borrowRepository.getAll()

    override suspend fun deleteBorrow(borrowID: Long): CoreResult<Boolean> {
        return try {
            borrowRepository.getById(borrowID) ?: return CoreResult.ClientError("Not Found")
            val deleted = borrowRepository.delete(borrowID)
            CoreResult.Success(deleted)
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun countBorrowPerWeek(): CoreResult<Map<LocalDate, Int>> = runCatching {
        borrowRepository.countBorrowPerWeek()
    }.fold(
        onSuccess = {
            CoreResult.Success(it)
        },
        onFailure = {
            println(it.printStackTrace())
            CoreResult.Failure(it.message.toString())
        }
    )

    override suspend fun analyticBorrow(
        date: LocalDate,
        period: Int
    ): CoreResult<Analyze> = runCatching {

        val areFieldBlank = period == 0 || date > LocalDate.now()
        val writeInput = period == 1 || period == 7 || period == 30 || period == 365

        if (areFieldBlank || !writeInput)
            return CoreResult.ClientError("Invalid data input.")

        val getCount = borrowRepository.countCurrentAndPreviousBorrow(date, period)
        val currentCount = getCount.currentValue
        val previousCount = getCount.previousValue
        val percentageChange: Float = if (previousCount == 0){
            if (currentCount == 0) 0f else 100f
        }else{
            ((currentCount - previousCount)).toFloat() / previousCount * 100f
        }
        Analyze(
            currentValue = currentCount,
            percentage = String.format("%.2f", percentageChange).toFloat()
        )
    }.fold(
        onSuccess = {
            value ->  CoreResult.Success(value)
        },
        onFailure = { e ->
            println(e.printStackTrace())
            CoreResult.Failure(e.message.toString())
        }
    )

    override suspend fun bookReturned(studentId: Long, bookId: String): CoreResult<Boolean> {
        return try {
            var borrowId = 0L
            val borrow = borrowRepository.findBorrowByStudentIdBookId(studentId, bookId)
            if (borrow.isEmpty()) return CoreResult.ClientError("Not found!")
            borrow.forEach {
                if (!it.isBringBack)
                    borrowId = it.borrowId!!
            }
            val update = borrowRepository.bookReturned(borrowId)
            CoreResult.Success(update)
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun findBorrowByStudentIdBookId(studentId: Long, bookId: String): List<Borrow> {
        return try {
            borrowRepository.findBorrowByStudentIdBookId(studentId, bookId)
        }catch (e: Exception){
            throw APIException(e.message)
        }
    }

    override fun overDueService(): Flow<Borrow> = borrowRepository.findOverDueBook()

    override suspend fun extendBorrow(borrowId: Long): CoreResult<Long> {
        return try {
            borrowRepository.getById(borrowId) ?: return CoreResult.ClientError("Not found")
            val id = borrowRepository.extendBorrow(borrowId)
            CoreResult.Success(id)
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun getBorrowDataEachMajor(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MajorAttendBorrowed> {
        try {
            val list = mutableListOf<MajorAttendBorrowed>()
            val data: Flow<Map<String, Int>> = borrowRepository.getAllBorrowForEachMajor(startDate, endDate)
            if (data.toList().isEmpty()) return emptyList()
            val totalBorrow = data
                .map { map ->
                    map.values.sum()
                }
                .reduce { accumulator, value ->
                    accumulator + value
                }
            data.collect{ map ->
                map.forEach { (k, v) ->
                    val percentage = (v.toFloat() / totalBorrow) * 100
                    list.add(MajorAttendBorrowed(
                        majorName = k,
                        times = v,
                        percentage = String.format("%.2f", percentage).toFloat())
                    )
                }
            }
            logger.info("$totalBorrow")
            return list
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override fun mostBorrow(startDate: LocalDate, endDate: LocalDate): Flow<MostBorrow> {
        return try {
            borrowRepository.getMostBorrow(startDate, endDate)
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun getBorrowAndReturn(
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<BorrowReturn> {
        return try {
            val borrow = borrowRepository.getAll().toList()
            val filterReturned = borrow.filter {
                it.isBringBack &&
                        (startDate == null || !it.giveBackDate.isBefore(startDate)) &&
                        (endDate == null || !it.giveBackDate.isAfter(endDate))
            }
            val filterBorrowed = borrow.filter {
                (startDate == null || !it.borrowDate.isBefore(startDate)) &&
                        (endDate == null || !it.borrowDate.isAfter(endDate))
            }

            val borrowByMonth = filterBorrowed.groupBy { YearMonth.from(it.borrowDate) }
            val returnByMonth = filterReturned.groupBy { YearMonth.from(it.giveBackDate) }
            val result = mutableListOf<BorrowReturn>()

            val allMonth = borrowByMonth.keys.union(returnByMonth.keys)

            logger.info("$filterBorrowed")

            allMonth.forEach{ month ->
                val borrowCount = borrowByMonth[month]?.size ?: 0
                val returnCount = returnByMonth[month]?.size ?: 0
                result.add(BorrowReturn(month, borrowCount, returnCount))
            }
            result
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun getBorrowDetail(): List<BorrowDetail> {
        return try {
            borrowRepository.getBorrowDetail()
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

}