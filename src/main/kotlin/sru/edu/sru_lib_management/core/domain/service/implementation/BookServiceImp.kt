/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto
import sru.edu.sru_lib_management.core.domain.dto.BookDto
import sru.edu.sru_lib_management.core.domain.dto.DonationDetailDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookEachCollege
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookIncome
import sru.edu.sru_lib_management.core.domain.dto.analytic.TotalBook
import sru.edu.sru_lib_management.core.domain.model.Books
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.core.domain.repository.BorrowRepository
import sru.edu.sru_lib_management.core.domain.repository.DonationRepository
import sru.edu.sru_lib_management.core.domain.service.BookService
import sru.edu.sru_lib_management.core.domain.service.CollegeService
import sru.edu.sru_lib_management.core.domain.service.LanguageService
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate
import sru.edu.sru_lib_management.utils.toBookDto
import sru.edu.sru_lib_management.utils.toFlowBookDto
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
@Component
class BookServiceImp(
    private val bookRepository: BookRepository,
    private val borrowRepository: BorrowRepository,
    private val languageService: LanguageService,
    private val collegeService: CollegeService,
    private val donationRepository: DonationRepository
): BookService {

    private val logger = LoggerFactory.getLogger(BookServiceImp::class.java)
    private val allBook = bookRepository.getAll()

    override fun getAllBooks(): Flow<BookDto> {
        return try {
            val book = bookRepository.getAll()
            val bookDtoFlow = runBlocking {
                book.toFlowBookDto<BookDto>()
            }
            bookDtoFlow
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun saveBook(books: List<BookDto>): CoreResult<List<BookDto>> {
        return try {
            val saved = books.mapNotNull { dto ->
                val areFieldsBlank = dto.bookId.isBlank() || dto.bookTitle.isBlank()
                val isHasID = bookRepository.getById(dto.bookId)
                if (isHasID != null || areFieldsBlank){
                    return@mapNotNull null
                }
                val checkId = bookRepository.getById(dto.bookId)
                if (checkId != null){
                    return CoreResult.ClientError("ID already exist.")
                }
                val book = Books(
                    bookId = dto.bookId,
                    bookTitle = dto.bookTitle,
                    bookQuan = dto.bookQuan,
                    languageId = dto.languageId,
                    collegeId = dto.collegeId,
                    genre = dto.genre,
                    isActive = true,
                    author = dto.author,
                    publicationYear = dto.publicationYear,
                    inactiveDate = null,
                    receiveDate = dto.receiveDate
                )
                bookRepository.save(entity = book)
                dto
            }
            if (saved.size != books.size)
                return CoreResult.ClientError("Some books could not be saved due to invalid data or duplicate IDs")
            CoreResult.Success(saved)
        }catch (e: Exception) {
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun updateBook(books: BookDto): CoreResult<BookDto> {
        return try {
            val checkBook = bookRepository.getById(books.bookId)
                ?: return CoreResult.ClientError("Book with ID ${books.bookId} not found!")

            logger.info("${checkBook.inactiveDate}")
            val bookUpdate = Books(
                bookId = books.bookId,
                bookTitle = books.bookTitle,
                bookQuan = books.bookQuan,
                languageId = books.languageId,
                collegeId = books.collegeId,
                genre = books.genre,
                isActive = checkBook.isActive,
                author = books.author,
                publicationYear = books.publicationYear,
                inactiveDate = checkBook.inactiveDate,
                receiveDate = books.receiveDate
            )

            val book = bookRepository.update(bookUpdate)
            CoreResult.Success(book.toBookDto())
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun getBook(bookId: String): CoreResult<BookDto> {
        return try {
            val book = bookRepository.getById(bookId)
                ?: return CoreResult.ClientError("Not found!")
            CoreResult.Success(book.toBookDto())
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun deleteBook(bookId: String): CoreResult<Boolean> {
        return try {
            bookRepository.getById(bookId) ?: return CoreResult.ClientError("Not found!")
            val delete = bookRepository.delete(bookId)
            if (!delete)
                return CoreResult.Failure("Unknown Error happen!")
            CoreResult.Success(true)
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun getAvailableBook(): CoreResult<List<BookAvailableDto>>
    = runCatching {
        bookRepository.bookAvailable()
    }.fold(
        onSuccess = {data ->
            CoreResult.Success(data)
        },
        onFailure = { e ->
            println(e.printStackTrace())
            CoreResult.Failure(e.message.toString())
        }
    )

    override fun currentAvailableBook(): Flow<Books> {
        return try {
            val allBorrowed = borrowRepository.getAll()
            val allBooks = bookRepository.getAll()

            allBooks.flatMapConcat { book ->
                allBorrowed
                    .filter { borrowed ->
                        val bool = borrowed.bookId == book.bookId
                        bool
                    }
                    .fold(book.bookQuan){ availableBook, borrowed ->
                        val adjustment = if (borrowed.isBringBack){
                            0
                        }else{ -borrowed.bookQuan }
                        val b = availableBook + adjustment
                        b
                    }
                    .let {availableQuan ->
                        if (availableQuan > 0){
                            flowOf(
                                Books(
                                    bookId = book.bookId,
                                    bookTitle = book.bookTitle,
                                    bookQuan = availableQuan,
                                    languageId = book.languageId,
                                    collegeId = book.collegeId,
                                    author = book.author,
                                    publicationYear = book.publicationYear,
                                    genre = book.genre,
                                    isActive = book.isActive,
                                    inactiveDate = book.inactiveDate,
                                    receiveDate = book.receiveDate
                                )
                            )
                        }else{
                            emptyFlow()
                        }
                    }
            }
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun moveToTrash(
        bookId: List<String>
    ): CoreResult<String> {
        return try {
            for (id in bookId){
                bookRepository.getById(id) ?: return CoreResult.ClientError("Not found!")
                val result = bookRepository.bookTrash(id, indoChinaDate())
                if (result != id){
                    return CoreResult.ClientError(result)
                }
            }
            CoreResult.Success("Success!")
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }

    override fun getBooksInTrash(): Flow<Books> {
        val book =  bookRepository.getAllBookInTrash()
            .catch { e ->
                logger.error("Error fetching books in trash", e)
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch books in trash")
            }
        logger.info("Service $book")
        return book
    }

    override suspend fun emptyTrash() {
        try {
            val date = indoChinaDate().minusDays(30)
            bookRepository.emptyTrash(date)
        }catch (e: Exception){
            throw Exception(e.message.toString())
        }
    }

    override suspend fun recoveryBooks(
        bookId: List<String>
    ): CoreResult<String> {
        return try {
            for (id in bookId){
                bookRepository.getById(id) ?: return CoreResult.ClientError("Not found book with this ID: $id")
                val result = bookRepository.recoveryBook(id)
                if (result != id)
                    return CoreResult.ClientError(result)
            }
            CoreResult.Success("Success")
        }catch (e: Exception){
            logger.info(e.message)
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun bookLanguage(): TotalBook {
        try {
            val languages = languageService.findAll().collectList().awaitSingle()
            val languageCountMap = mutableMapOf<String, Int>()
            for (book in allBook.toList()){
                val languageId = book.languageId
                val currentQuan = languageCountMap.getOrDefault(languageId, 0)
                languageCountMap[languageId] = currentQuan + book.bookQuan
            }
            val languageNameMap = languageCountMap.mapKeys { (languageId, _) ->
                languages.find { it.languageId == languageId }?.languageName ?: languageId
            }
            val totalBook = allBook.toList().sumOf { it.bookQuan }

            return TotalBook(totalBook = totalBook, bookEachLanguage = languageNameMap)
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun getBookDataForEachCollege(
        startDate: YearMonth?, endDate: YearMonth?
    ): List<BookEachCollege> {
        return try {
            val collegeNameList = collegeService.findAll().collectList().awaitSingle()
            val collegeMap = collegeNameList.associate { it.collegeId to it.collegeName }
            val donation: List<DonationDetailDto> = donationRepository.getDonationDetail().toList()

            val donationMap = donation.associateBy { it.bookId }

            allBook.toList()
                .filter {
                    val bookDate = it.receiveDate ?: donationMap[it.bookId]?.donateDate
                    if (startDate != null && endDate != null && bookDate != null){
                        val bookYearMonth = YearMonth.from(bookDate)
                        (bookYearMonth.isAfter(startDate) || bookYearMonth == startDate) &&
                                (bookYearMonth.isBefore(endDate) || bookYearMonth == endDate)
                    } else true
                }
                .groupBy { it.collegeId }
                .map{ (collegeId, book) ->
                    val bookEachCollege = book.groupBy { it.languageId }
                        .mapValues { (_, langBook) ->
                            langBook.sumOf { it.bookQuan }
                        }
                    BookEachCollege(collegeMap[collegeId] ?: collegeId, bookEachCollege)
                }
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun getBookIncome(
        startMonth: YearMonth?,
        endMonth: YearMonth?
    ): List<BookIncome> {
        return try {
            val allUniversityFunding = bookRepository.universityFunding()
            val allDonation = bookRepository.allBookDonation()

            val combineMap = mutableMapOf<YearMonth, BookIncome>()
            allUniversityFunding.forEach {
                combineMap[it.month] = BookIncome(
                    month = it.month.toString(),
                    donation = 0,
                    universityFunding = it.bookCount
                )
            }

            allDonation.forEach { donation ->
                combineMap.merge(donation.month, BookIncome(
                    month =donation.month.toString(),
                    donation = donation.bookCount,
                    universityFunding = 0
                )){exist, new ->
                    exist.copy(donation = new.donation)
                }
            }

            val filterList = combineMap.values
                .filter {
                    val currentMonth = YearMonth.parse(it.month!!)
                    (startMonth == null || currentMonth >= startMonth) && (endMonth == null || currentMonth <= endMonth)
                }.sortedBy { it.month }
            //combineMap.values.sortedBy { YearMonth.parse(it.month.toString()) }
            filterList
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override suspend fun aboutBookData(): Map<String, Int> {
        return try {
            val booksList = bookRepository.getAll().toList()
            val donationList = donationRepository.getDonationDetail().toList()
            val allBorrowList = borrowRepository.getAll().toList()

            val totalBook = booksList.sumOf { it.bookQuan }
            val totalDonation = donationList.sumOf { it.bookQuan }

            val totalBorrow = allBorrowList.sumOf { it.bookQuan }
            val totalExp = allBorrowList
                .filter {
                    !it.isBringBack && it.giveBackDate.isBefore(indoChinaDate())
                }.sumOf { it.bookQuan }

            val todayBorrow = allBorrowList.filter { it.borrowDate == indoChinaDate() }.sumOf { it.bookQuan }
            val todayReturn = allBorrowList
                .filter { it.isBringBack && it.giveBackDate == indoChinaDate() }
                .sumOf { it.bookQuan }
            mapOf(
                "totalBook" to totalBook,
                "totalDonation" to totalDonation,
                "totalBorrow" to totalBorrow,
                "totalExp" to totalExp,
                "todayBorrowed" to todayBorrow,
                "todayReturned" to todayReturn,
            )
        }catch (e: Exception){
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }
}
