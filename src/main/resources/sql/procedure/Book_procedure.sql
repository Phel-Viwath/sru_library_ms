/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

/*
* Note if you want to use local database please use sru_library
*/

#USE sru_library;

#Get available book
CREATE PROCEDURE if not exists GetAvailableBook()
BEGIN
    SELECT l.language_name,
           SUM(b.bookQuan) as total_book,
           SUM(b.bookQuan - IFNULL(bb.borrowed_count, 0)) as available_books
        FROM books b
            INNER JOIN language l on b.language_id = l.language_id
            LEFT JOIN (
                SELECT book_id, count(*) as borrowed_count
                From borrow_books WHERE is_bring_back = FALSE
                GROUP BY book_id
            ) bb ON b.book_id = bb.book_id
        GROUP BY l.language_name;
end;

#Book Trash
create procedure MoveToTrash(
    IN bookId VARCHAR(10),
    IN currentDate date
)begin
    UPDATE books set isActive = false, inactiveDate = currentDate where book_id = bookId;
end;

# empty trash in 30 days
Create Procedure EmptyTrash(IN deleteDate DATE)
BEGIN
    DELETE From books where isActive = false and inactiveDate <= deleteDate - interval 30 day;
end;

CREATE PROCEDURE RecoverBook(in bookId varchar(10))
begin
    UPDATE books set isActive = true, inactiveDate = NULL where book_id = bookId;
end;

# filter book
create procedure FilterBook(
    IN genre VARCHAR(100),
    IN collegeName VARCHAR(100),
    IN languageName VARCHAR(100),
    IN bookTitle varchar(100)
)BEGIN
    SELECT * FROM books b
        JOIN colleges c on b.college_id = c.college_id
        JOIN language l on b.language_id = l.language_id
        where (genre is null or b.genre like genre)
        and ( collegeName is null or c.college_name like collegeName)
        and ( languageName is null or l.language_name like languageName)
        and ( bookTitle is null or b.book_title like bookTitle)
        and ( bookTitle is null or b.book_title like CONCAT('%', bookTitle, '%'));
end;

CREATE PROCEDURE CheckTrash(
    in curren_date DATE
)BEGIN
    SELECT * From books
            WHERE isActive = false
            AND inactiveDate <= curren_date - INTERVAL 25 DAY
            OR inactiveDate <= curren_date - INTERVAL 26 DAY
            OR inactiveDate <= curren_date - INTERVAL 27 DAY
            OR inactiveDate <= curren_date - INTERVAL 28 DAY
            OR inactiveDate <= curren_date - INTERVAL 29 DAY;
end;

CREATE PROCEDURE CountBookForEachCollege()
BEGIN
    SELECT
        c.college_name AS College,
        SUM(IF(b.language_id = 'KH', b.bookQuan, 0)) AS KH,
        SUM(IF(b.language_id = 'ENG', b.bookQuan, 0)) AS ENG
    FROM
        books b
            JOIN
        colleges c ON b.college_id = c.college_id
    WHERE
        b.language_id IN ('KH', 'ENG')
    GROUP BY
        c.college_name
    ORDER BY
        c.college_name;
end;
