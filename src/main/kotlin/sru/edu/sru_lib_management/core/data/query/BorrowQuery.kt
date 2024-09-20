/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.query

import org.springframework.stereotype.Component

@Component
object BorrowQuery {
    const val SAVE_BORROW_QUERY: String = """
        INSERT INTO borrow_books (book_id, student_id, book_quan, borrow_date, give_back_date, is_bring_back, is_extend)
        VALUES (:bookId, :studentId, :bookQuan, :borrowDate, :giveBackDate, :isBringBack, :isExtend);
    """
    const val UPDATE_BORROW_QUERY: String =
        "UPDATE borrow_books set book_id = :bookId, student_id = :studentId, borrow_date = :borrowDate, " +
                "give_back_date = :givBackDate, is_bring_back = :isBringBack, is_extend = :isExtend WHERE borrow_id = :borrowId;"
    const val DELETE_BORROW_QUERY: String = "DELETE borrow_books WHERE borrow_id = :borrowId;"
    const val GET_BORROWS_QUERY: String = "SELECT * FROM borrow_books;"
    const val GET_BORROW_QUERY: String = "SELECT * FROM borrow_books WHERE borrow_id = :borrowId;" ////

    const val BOOK_RETURN = "Update borrow_books set give_back_date = :givBackDate, is_bring_back = 1 Where borrow_id = :borrowId;"
    const val FIND_BORROW_BY_STUDENT_ID_BOOK_ID = "Select * from borrow_books WHERE book_id = :bookId and student_id = :studentId;"
}
