/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import sru.edu.sru_lib_management.core.domain.dto.BookDto
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDto
import sru.edu.sru_lib_management.core.domain.model.Attend
import sru.edu.sru_lib_management.core.domain.model.Books

suspend fun <T> Flow<T>.reversFlow(): Flow<T>{
    // convert to list
    val list = this.toList()
    // reverse list
    val reverseList = list.asReversed()

    return flow {
        for (i in reverseList){
            emit(i)
        }
    }
}

suspend fun <T> Flow<Books>.toFlowBookDto(): Flow<BookDto>{
    val list = this.toList()
    return flow {
        for (i in list){
            emit(i.toBookDto())
        }
    }
}

// DTO
fun Books.toBookDto(): BookDto{
    return BookDto(
        bookId = this.bookId,
        bookTitle = this.bookTitle,
        bookQuan = this.bookQuan,
        languageId = this.languageId,
        collegeId = this.collegeId,
        author = this.author,
        publicationYear = this.publicationYear,
        genre = this.genre,
        receiveDate = this.receiveDate
    )
}
// to student attend
//fun Attend.toStudentAttend(): StudentAttendDto = StudentAttendDto(
//    attendId = attendId!!,
//    studentId = studentId!!,
//    entryTimes = entryTimes,
//    exitingTimes = exitingTimes,
//    purpose = purpose,
//    date = date
//)
// to staff attend


fun String.checkEntryId(): Any{
    val id = this
    return if (id.all { it.isDigit() })
        id.toLong()
    else
        id
}