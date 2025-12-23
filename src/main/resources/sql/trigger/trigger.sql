/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

CREATE TRIGGER remove_from_blacklist_after_return
    AFTER UPDATE ON borrow_books
    FOR EACH ROW
BEGIN
    # Check if the book is brought back
    IF NEW.is_bring_back = TRUE THEN
        # Delete it from blacklist if the student is on it
        DELETE FROM blacklist
        WHERE student_id = NEW.student_id
          AND book_id = NEW.book_id;
    END IF;
end;

# Delete Donator and Book after donation deleted
CREATE TRIGGER delete_book_after_donation_delete
    After delete on donation FOR EACH ROW
BEGIN
    DECLARE book_count int;
    #Check if the book is update or donated by another donator
    SELECT COUNT(*) INTO book_count from donation where book_id = OLD.book_id;

    if book_count = 0 THEN
        DELETE from books where books.book_id = OLD.book_id;
    end if;
end;

CREATE TRIGGER delete_donator_after_donation_delete
    After Delete on donation For each row
BEGIN
    Declare donator_count Int;
    #Check if the book is update or donated by another donator
    Select Count(*) Into donator_count from donation where donator_id = OLD.donator_id;

    # delete
    If donator_count = 0 Then
        Delete From donator where donator.donator_id = OLD.donator_id;
    end if;
end;

