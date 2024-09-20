/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.repository

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sru.edu.sru_lib_management.core.data.query.BookQuery.DELETE_BOOK_QUERY
import sru.edu.sru_lib_management.core.data.query.BookQuery.GET_BOOKS_QUERY
import sru.edu.sru_lib_management.core.data.query.BookQuery.GET_BOOK_QUERY
import sru.edu.sru_lib_management.core.data.query.BookQuery.SAVE_BOOK_QUERY
import sru.edu.sru_lib_management.core.data.query.BookQuery.SEARCH_BOOK_QUERY
import sru.edu.sru_lib_management.core.data.query.BookQuery.UPDATE_BOOK_QUERY
import sru.edu.sru_lib_management.core.domain.dto.BookAvailableDto
import sru.edu.sru_lib_management.core.domain.dto.FundCount
import sru.edu.sru_lib_management.core.domain.model.Books
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


@Component
class BookRepositoryImp(
    private val client: DatabaseClient,
) : BookRepository {

    private val logger = LoggerFactory.getLogger(BookRepositoryImp::class.java)

    suspend fun searchBooks(criteria: Map<String, Any?>): List<Books> {
        val query = StringBuilder(SEARCH_BOOK_QUERY)
        val params = mutableMapOf<String, Any?>()

        criteria.forEach { (k, v) ->
            if (v != null){
                query.append("AND $k = :$k")
                params[k] = v
            }
        }
        val execute = client.sql(query.toString())
        params.forEach {(k,v) ->
            execute.bind(k, v)
        }
        return execute.map { row: Row, _ ->
            row.rowMapping()
        }.all().collectList().awaitSingle()
    }

    override suspend fun bookAvailable(): List<BookAvailableDto> {
        return client.sql("CALL GetAvailableBook()")
            .map { row ->
                BookAvailableDto(
                    language = row.get("language_name", String::class.java)!!,
                    totalBook = row.get("total_book", Int::class.java)!!,
                    available = row.get("available_books", Int::class.java)!!
                )
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun bookTrash(bookId: String, date: LocalDate): String {
        val map = mapOf(
            "bookId" to bookId,
            "date" to date
        )
        val rowEffect = client.sql("CALL MoveToTrash(:bookId, :date)")
            .bindValues(map)
            .fetch()
            .awaitRowsUpdated()
        return if (rowEffect > 0)
            bookId
        else "error move book with ID: $bookId"
    }

    override suspend fun recoveryBook(
        bookId: String,
    ): String{
        val effected = client.sql("CALL RecoverBook(:bookId)")
            .bind("bookId", bookId)
            .fetch()
            .awaitRowsUpdated()
        return if (effected > 0)
            bookId
        else "error recovering book with ID: $bookId"
    }

    override suspend fun emptyTrash(deleteDate: LocalDate) {
        client.sql("CALL EmptyTrash(:deleteDate)")
            .bind("deleteDate",deleteDate)
            .fetch()
            .awaitOneOrNull()
    }

    override fun getAllBookInTrash(): Flow<Books> {
        return client.sql("SELECT * FROM books WHERE isActive = false;")
            .map { row: Row, _ ->
                row.rowMapping()
            }
            .all()
            .asFlow()
    }

    override suspend fun alertTrashMessage(currentDate: LocalDate): List<Books> {
        return client.sql("CALL CheckTrash(:currentDate)")
            .map { row: Row, _ ->
                row.rowMapping()
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun universityFunding(): List<FundCount> {
        return client.sql(""" SELECT DATE_FORMAT(received_date, '%Y-%m') AS month, COUNT(*) AS funded_books_count
            FROM books
            WHERE received_date IS NOT NULL AND book_id NOT IN (SELECT book_id FROM donation)
            GROUP BY month ORDER BY month;
        """)
            .map { row ->
                FundCount(
                    YearMonth.parse(row.get("month", String::class.java)!!, DateTimeFormatter.ofPattern("yyyy-MM")),
                    row.get("funded_books_count", Int::class.java)!!
                )
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun allBookDonation(): List<FundCount> {
        return client.sql("""SELECT DATE_FORMAT(donate_date, '%Y-%m') AS month,
            COUNT(*) AS donated_books_count
            FROM donation GROUP BY month ORDER BY month;
        """).map { row ->
                FundCount(
                    YearMonth.parse(row.get("month", String::class.java)!!, DateTimeFormatter.ofPattern("yyyy-MM")),
                    row.get("donated_books_count", Int::class.java)!!,
                )
            }
            .all()
            .collectList()
            .awaitSingle()
    }

    @Transactional
    override suspend fun save(entity: Books): Books {
        client.sql(SAVE_BOOK_QUERY)
            .bindParam(paramMap(entity))
            .await()
        return entity
    }

    override suspend fun update(entity: Books): Books {
        client.sql(UPDATE_BOOK_QUERY)
            .bindParam(paramMap(entity))
            .fetch()
            .awaitRowsUpdated()
        return entity
    }


    override suspend fun getById(id: String): Books? {
        return client.sql(GET_BOOK_QUERY)
            .bind("bookId", id)
            .map { row: Row, _ ->
                row.rowMapping()
            }
            .awaitOneOrNull()
    }

    override fun getAll(): Flow<Books> {
        return client.sql(GET_BOOKS_QUERY)
            .map { row: Row, _ ->
                row.rowMapping()
            }.flow()
    }

    override suspend fun delete(id: String): Boolean {
        val rowEffect = client.sql(DELETE_BOOK_QUERY)
            .bind("bookId", id)
            .fetch()
            .awaitRowsUpdated()
        return rowEffect > 0
    }

    private fun Row.rowMapping(): Books = Books(
        this.get("book_id", String::class.java)!!,
        this.get("book_title", String::class.java)!!,
        this.get("bookQuan", Int::class.java)!!,
        this.get("language_id", String::class.java)!!,
        this.get("college_id", String::class.java)!!,
        this.get("author", String::class.java),
        this.get("publication_year", Int::class.java),
        this.get("genre", String::class.java)!!,
        this.get("received_date", LocalDate::class.java),
        this.get("isActive", Boolean::class.java)!!,
        this.get("inactiveDate", LocalDate::class.java)
    )

    private fun paramMap(books: Books): Map<String, Any?>{
        return mapOf(
            "bookId" to books.bookId,
            "bookTitle" to books.bookTitle,
            "bookQuan" to books.bookQuan,
            "languageId" to books.languageId,
            "collegeId" to books.collegeId,
            "genre" to books.genre,
            "publicationYear" to books.publicationYear,
            "author" to books.author,
            "receiveDate" to books.receiveDate,
            "isActive" to books.isActive,
            "inactiveDate" to books.inactiveDate
        )
    }

    private fun DatabaseClient.GenericExecuteSpec.bindParam(param: Map<String, Any?>)
            : DatabaseClient.GenericExecuteSpec{
        var statement = this
        for ((key, value) in param) {
            if (value != null) {
                statement = statement.bind(key, value)
            } else {
                val valueType: Class<*> = getValueKey(key)
                statement = statement.bindNull(key, valueType)
            }
        }
        return statement
    }

    private fun getValueKey(key: String): Class<*> {
        return when(key){
            "bookId", "bookTitle", "languageId", "collegeId", "author", "genre" -> String::class.java
            "bookQuan", "publicationYear" -> Int::class.java
            "isActive" -> Boolean::class.java
            "inactiveDate", "receiveDate" -> LocalDate::class.java
            else -> String::class.java
        }
    }


}
