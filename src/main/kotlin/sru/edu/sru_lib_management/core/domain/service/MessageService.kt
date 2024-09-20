/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.core.domain.dto.BookDto
import sru.edu.sru_lib_management.utils.toBookDto
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.utils.IndochinaDateTime.indoChinaDate

@Service
class MessageService @Autowired constructor(
    private val bookRepository: BookRepository
) {
    val listBookDto: MutableList<BookDto>? = null
    suspend fun alertRecoveryBook(): List<BookDto>{
        val books = bookRepository.alertTrashMessage(indoChinaDate())
        books.forEach { book ->
            val bookDto = book.toBookDto()
            listBookDto?.add(bookDto)
        }
        return listBookDto!!
    }

}