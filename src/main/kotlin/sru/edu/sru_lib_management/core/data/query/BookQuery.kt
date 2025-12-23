/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.query

object BookQuery {

    const val SAVE_BOOK_QUERY ="""
            INSERT INTO books (book_id, book_title, bookQuan, language_id, college_id, genre, isActive, author, publication_year, inactiveDate, received_date)
            VALUES (:bookId, :bookTitle, :bookQuan, :languageId, :collegeId, :genre, :isActive, :author, :publicationYear, :inactiveDate, :receiveDate);
        """

    const val UPDATE_BOOK_QUERY = """
            UPDATE books SET book_title = :bookTitle, bookQuan = :bookQuan, language_id = :languageId, 
            college_id = :collegeId, genre = :genre, author = :author, publication_year = :publicationYear, 
            isActive = :isActive, inactiveDate = :inactiveDate, received_date = :receiveDate
        WHERE book_id = :bookId;
    """

    const val DELETE_BOOK_QUERY = "DELETE FROM books WHERE book_id = :bookId;"

    const val GET_BOOK_QUERY = "SELECT * FROM books WHERE book_id = :bookId;"

    const val GET_BOOKS_QUERY = "SELECT * FROM books;"

    const val GET_AVAILABLE_BOOK_QUERY = """
        SELECT
            b.book_id,
            b.book_title,
            b.bookQuan - COALESCE(SUM(bb.book_quan), 0) AS bookQuan,
            b.language_id,
            b.college_id,
            b.author,
            b.publication_year,
            b.genre,
            b.received_date,
            b.isActive,
            b.inactiveDate
        FROM
            books b
                LEFT JOIN
            borrow_books bb ON b.book_id = bb.book_id AND bb.is_bring_back = FALSE
        GROUP BY
            b.book_id, b.book_title, b.bookQuan
        HAVING
            b.bookQuan - COALESCE(SUM(bb.book_quan), 0) > 0
        ORDER BY
            b.book_title;
    """


    // ======== ================== =================================
    // Sponsor query
    const val SAVE_DONATOR = """
        INSERT INTO donator(donator_name)
        VALUES (:sponsorName);
    """
    const val UPDATE_DONATOR = """
        Update donator set donator_name = :donatorName 
        WHERE donator_id = :donatorId;
    """
    const val DELETE_DONATOR = "Delete * from donator Where donator_id = :donatorId;"
    const val GET_DONATOR = "Select * from donator Where donator_id = :donatorId;"
    const val GET_ALL_DONATOR = "Select * from donator;"
    // ======== ================== =================================
    // Book sponsorship query

    const val SAVE_DONATION = """
        Insert into donation(book_id, donator_id, donate_date)
        Value (:bookId, :donatorId, :donateDate);
    """
    const val DELETE_DONATION = "Delete from donation Where donator_id = :donatorId or bookId = :bookId"

    const val UPDATE_DONATION_QUERY = """
        UPDATE donation 
        SET donate_date = :donateDate 
        WHERE book_id = :bookId AND donator_id = :donatorId
    """

}