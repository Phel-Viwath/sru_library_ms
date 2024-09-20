/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

USE sru_library;
# Get sponsor book by custom date
Create PROCEDURE if not exists CountSponsorByPeriod(in date DATE, in period INT)
BEGIN
    DECLARE current_date_count INT;
    DECLARE previous_date DATE;
    DECLARE previous_date_count INT;
    DECLARE current_7_day_count INT;
    DECLARE previous_7_day_count INT;
    DECLARE current_month_count INT;
    DECLARE previous_month_count INT;
    DECLARE current_year_count INT;
    DECLARE previous_year_count INT;

    SET previous_date = date - interval 1 day;

    IF period = 1 Then
        # Select current date
        SELECT COUNT(*) INTO current_date_count
        FROM donation WHERE donate_date = date;
        # Select previous day
        SELECT COUNT(*) INTO previous_date_count
        FROM donation WHERE donate_date = previous_date;
        # Return the results
        SELECT current_date_count as current_value,
               previous_date_count as previous_value;

    ELSEIF period = 7 Then
        #Select current week
        SELECT COUNT(*) INTO current_7_day_count
        FROM donation
        WHERE donate_date BETWEEN date - INTERVAL 6 day and date;
        # Select previous week
        SELECT COUNT(*) INTO previous_7_day_count
        FROM donation
        WHERE donate_date BETWEEN date - INTERVAL 13 day and date - INTERVAL 7 day;
        # Return the results
        SELECT current_7_day_count as current_value,
               previous_7_day_count as previous_value;

    ELSEIF period = 30 Then
        # Select count for current month
        SELECT COUNT(*) INTO current_month_count
        FROM donation
        WHERE YEAR(donate_date) = YEAR(date) AND MONTH(donate_date) = MONTH(date);
        # Select count for previous month
        SELECT COUNT(*) INTO previous_month_count
        FROM donation
        WHERE YEAR(donate_date) = YEAR(date - INTERVAL 1 MONTH) AND MONTH(donation.donate_date) = MONTH(date - INTERVAL 1 MONTH);
        # Return Value
        SELECT current_month_count as current_value,
               previous_month_count as previous_value;
    ELSEIF period = 365 Then
        # Select count for current year
        SELECT COUNT(*) INTO current_year_count
        FROM donation
        WHERE donate_date BETWEEN date - INTERVAL 364 day and date;
        # Select count for previous year
        SELECT COUNT(*) INTO previous_year_count
        FROM donation
        WHERE donate_date between date - INTERVAL ((365*2) - 1) day and date - Interval 365 day ;
        # Return Value
        SELECT current_year_count as current_value,
               previous_year_count as previous_value;
    END if;
end;

# Get Donation Detail
CREATE PROCEDURE DonationDetail()
BEGIN
    SELECT
        b.book_id as bookId,
        d.donator_name as donatorName,
        b.book_title as bookTitle,
        b.bookQuan as bookQuan,
        l.language_name as languageName,
        c.college_name as collegeName,
        b.author as author,
        b.publication_year as publication_year,
        b.genre as genre,
        do.donate_date as donateDate
    from
        donation do inner join
        donator d on do.donator_id = d.donator_id
        inner join books b on do.book_id = b.book_id
        INNER JOIN language l on b.language_id = l.language_id
        INNER join colleges c on b.college_id = c.college_id;
end;


################################### Get yearly donation detail ##################################
CREATE PROCEDURE YearlyDonationDetail(
    IN yearType varchar(10)
)BEGIN
    Declare targetYear int;
    if yearType = 'current' then
        Set targetYear = YEAR(CURDATE());
    ElseIf yearType = 'previous' then
        Set targetYear = YEAR(CURDATE()) - 1;
    else
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid year type. Use "current" or "previous".';
    End if;
    SELECT
        b.book_id as bookId,
        d.donator_name as donatorName,
        b.book_title as bookTitle,
        b.bookQuan as bookQuan,
        l.language_name as languageName,
        c.college_name as collegeName,
        b.author as author,
        b.publication_year as publication_year,
        b.genre as genre,
        do.donate_date as donateDate
    from
        donation do inner join
        donator d on do.donator_id = d.donator_id
                    inner join books b on do.book_id = b.book_id
                    INNER JOIN language l on b.language_id = l.language_id
                    INNER join colleges c on b.college_id = c.college_id
    Where YEAR(do.donate_date) = targetYear;
end;










