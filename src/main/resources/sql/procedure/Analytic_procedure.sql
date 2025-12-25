/*
CREATE PROCEDURE IF NOT EXISTS PurposeCount(
    IN startDate DATE,
    IN endDate DATE,
    IN major VARCHAR(100)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    -- Validate input parameters
    IF startDate IS NULL OR endDate IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Start date and end date cannot be null';
    END IF;

    IF startDate > endDate THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Start date cannot be greater than end date';
    END IF;

    -- Main query to categorize and count purposes
    SELECT
        purpose_category,
        SUM(purpose_count) as total_count
    FROM (
         SELECT
             CASE
                 WHEN LOWER(a.purpose) LIKE '%reading%' OR LOWER(a.purpose) = 'read' THEN 'Reading'
                 WHEN LOWER(a.purpose) LIKE '%pc%' OR LOWER(a.purpose) LIKE '%computer%' OR LOWER(a.purpose) LIKE '%use pc%' THEN 'Use PC'
                 WHEN LOWER(a.purpose) LIKE '%assignment%' or LOWER(a.purpose) LIKE '%ass%' THEN 'Assignment'
                 ELSE 'Other'
                 END as purpose_category,
             COUNT(*) as purpose_count
         FROM attend a
                  LEFT JOIN students s ON a.student_id = s.student_id
                  LEFT JOIN majors m ON s.major_id = m.major_id
         WHERE
             a.date BETWEEN startDate AND endDate
           AND (
             major IS NULL
                 OR major = ''
                 OR m.major_name = major
                 OR a.sru_staff_id IS NOT NULL  -- Include libraryStaff entries when major filter is applied
             )
         GROUP BY
             purpose_category,
             a.purpose
     ) as categorized_purposes
    GROUP BY purpose_category
    ORDER BY
        CASE purpose_category
            WHEN 'Reading' THEN 1
            WHEN 'Use PC' THEN 2
            WHEN 'Other' THEN 3
        END;

END;*/

CREATE PROCEDURE PurposeCount(
    IN startDate DATE,
    IN endDate DATE,
    IN majorName VARCHAR(100)
)
BEGIN
    -- ============================
    -- Validate input
    -- ============================
    IF startDate IS NULL OR endDate IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Start date and end date cannot be null';
    END IF;

    IF startDate > endDate THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Start date cannot be greater than end date';
    END IF;

    -- ============================
    -- Main query
    -- ============================
    SELECT
        purpose_category,
        COUNT(*) AS total_count
    FROM (
             SELECT
                 CASE
                     WHEN LOWER(a.purpose) LIKE '%reading%'
                         OR LOWER(a.purpose) = 'read'
                         THEN 'Reading'

                     WHEN LOWER(a.purpose) LIKE '%pc%'
                         OR LOWER(a.purpose) LIKE '%computer%'
                         OR LOWER(a.purpose) LIKE '%use pc%'
                         THEN 'Use PC'

                     WHEN LOWER(a.purpose) LIKE '%assignment%'
                         OR LOWER(a.purpose) LIKE '%ass%'
                         THEN 'Assignment'

                     ELSE 'Other'
                     END AS purpose_category
             FROM attend a
                      JOIN visitors v ON a.visitor_id = v.visitor_id
                      LEFT JOIN students s ON v.student_id = s.student_id
                      LEFT JOIN majors m ON s.major_id = m.major_id

             WHERE a.attend_date BETWEEN startDate AND endDate
               AND (
                 majorName IS NULL
                     OR majorName = ''
                     OR m.major_name = majorName
                     OR v.visitor_type = 'SRU_STAFF'
                 )
         ) categorized
    GROUP BY purpose_category
    ORDER BY
        CASE purpose_category
            WHEN 'Reading' THEN 1
            WHEN 'Use PC' THEN 2
            WHEN 'Assignment' THEN 3
            ELSE 4
            END;
END;

CREATE PROCEDURE IF NOT EXISTS GetBookForEachCollege(
    IN startMonth VARCHAR(7),  -- format: 'YYYY-MM'
    IN endMonth VARCHAR(7)     -- format: 'YYYY-MM'
)
BEGIN
    DECLARE startDate DATE;
    DECLARE endDate DATE;

    -- Convert YYYY-MM to DATE (start of month)
    IF startMonth IS NOT NULL THEN
        SET startDate = STR_TO_DATE(CONCAT(startMonth, '-01'), '%Y-%m-%d');
    ELSE
        SET startDate = NULL;
    END IF;

    -- Convert YYYY-MM to DATE (end of month using LAST_DAY)
    IF endMonth IS NOT NULL THEN
        SET endDate = LAST_DAY(STR_TO_DATE(CONCAT(endMonth, '-01'), '%Y-%m-%d'));
    ELSE
        SET endDate = NULL;
    END IF;

    SELECT
        c.college_name AS collegeName,
        COALESCE(l.language_name, 'Unknown') AS language,
        SUM(b.bookQuan) AS bookCount
    FROM books b
             JOIN colleges c ON b.college_id = c.college_id
             LEFT JOIN language l ON b.language_id = l.language_id
             LEFT JOIN donation d ON b.book_id = d.book_id
    WHERE (
        (startDate IS NOT NULL AND endDate IS NOT NULL AND (
            (b.received_date IS NOT NULL AND b.received_date BETWEEN startDate AND endDate)
                OR
            (d.donate_date IS NOT NULL AND d.donate_date BETWEEN startDate AND endDate)
            ))
            OR (startDate IS NULL AND endDate IS NULL)
        )
      AND b.isActive = TRUE
    GROUP BY c.college_name, l.language_name
    ORDER BY c.college_name, l.language_name;
END;

CREATE PROCEDURE IF NOT EXISTS GetBookLanguage()
BEGIN
    SELECT
        l.language_name AS languageName,
        SUM(b.bookQuan) AS totalBookPerLanguage
    FROM books b
             JOIN language l ON b.language_id = l.language_id
    WHERE b.isActive = true
    GROUP BY b.language_id;
end;

CREATE PROCEDURE IF NOT EXISTS GetBookIncome(
    IN startMonth VARCHAR(7),  -- e.g. '2024-01'
    IN endMonth VARCHAR(7)     -- e.g. '2024-03'
)
BEGIN

    -- Declare actual date range
    DECLARE startDate DATE;
    DECLARE endDate DATE;

    -- Exit handler for SQL errors
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            RESIGNAL;
        END;

    -- Convert YearMonth to DATE range
    IF startMonth IS NOT NULL THEN
        SET startDate = STR_TO_DATE(CONCAT(startMonth, '-01'), '%Y-%m-%d');
    ELSE
        SET startDate = NULL;
    END IF;

    IF endMonth IS NOT NULL THEN
        SET endDate = LAST_DAY(STR_TO_DATE(CONCAT(endMonth, '-01'), '%Y-%m-%d'));
    ELSE
        SET endDate = NULL;
    END IF;

    -- Drop if exists
    DROP TEMPORARY TABLE IF EXISTS temp_book_income;
    -- Create temp table
    CREATE TEMPORARY TABLE temp_book_income (
        month VARCHAR(7) PRIMARY KEY,
        donation INT DEFAULT 0,
        university_funding INT DEFAULT 0
    );

    -- Insert university-funded books
    INSERT INTO temp_book_income (month, university_funding)
    SELECT
        DATE_FORMAT(received_date, '%Y-%m') AS month,
        COUNT(*) AS university_funding
    FROM books
    WHERE received_date IS NOT NULL
      AND book_id NOT IN (SELECT book_id FROM donation)
      AND (startDate IS NULL OR received_date >= startDate)
      AND (endDate IS NULL OR received_date <= endDate)
    GROUP BY DATE_FORMAT(received_date, '%Y-%m');

    -- Insert or update donation-funded books
    INSERT INTO temp_book_income (month, donation)
    SELECT
        DATE_FORMAT(donate_date, '%Y-%m') AS month,
        COUNT(*) AS donation
    FROM donation
    WHERE (startDate IS NULL OR donate_date >= startDate)
      AND (endDate IS NULL OR donate_date <= endDate)
    GROUP BY DATE_FORMAT(donate_date, '%Y-%m')
    ON DUPLICATE KEY UPDATE donation = VALUES(donation);

    -- Final result
    SELECT
        month,
        COALESCE(donation, 0) AS donation,
        COALESCE(university_funding, 0) AS university_funding
    FROM temp_book_income
    ORDER BY month;
END;

/*CREATE PROCEDURE IF NOT EXISTS CountDuration(
    IN startDate DATE,  -- Start date or NULL
    IN endDate DATE     -- End date or NULL
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    -- Define time constants (in TIME format)
    SET @SEVEN_AM = TIME('07:00:00');
    SET @ELEVEN_AM = TIME('11:00:00');
    SET @TWO_PM = TIME('14:00:00');
    SET @FIVE_PM = TIME('17:00:00');
    SET @FIVE_THIRTY_PM = TIME('17:30:00');
    SET @SEVEN_THIRTY_PM = TIME('19:30:00');

    SELECT
        sad.student_id AS studentId,
        sad.studentName,
        sad.majorName AS major,
        sad.degree_level AS degree,
        sad.generation,
        SUM(
            IF(sad.exitingTime IS NULL, 0, TIMESTAMPDIFF(MINUTE, sad.entryTimes, sad.exitingTime))
        ) AS totalTimeSpent
    FROM (
             -- Get student attend details with proper filtering
             SELECT
                 a.student_id,
                 s.student_name AS studentName,
                 m.major_name AS majorName,
                 dl.degree_level,
                 s.generation,
                 a.entry_time AS entryTimes,
                 a.exit_time AS exitingTime
             FROM attend a
                      INNER JOIN students s ON a.student_id = s.student_id
                      INNER JOIN majors m ON s.major_id = m.major_id
                      INNER JOIN degree_level dl ON s.degree_level_id = dl.degree_level_id
             WHERE a.student_id IS NOT NULL
               AND (startDate IS NULL OR a.attend_date >= startDate)
               AND (endDate IS NULL OR a.attend_date <= endDate)
         ) AS sad
    WHERE sad.exitingTime IS NOT NULL
      -- Filter out invalid exit times (keep only valid library hours)
      -- Extract time part from timestamp for comparison
      AND (
        (TIME(sad.exitingTime) BETWEEN @SEVEN_AM AND @ELEVEN_AM) OR
        (TIME(sad.exitingTime) BETWEEN @TWO_PM AND @FIVE_PM) OR
        (TIME(sad.exitingTime) BETWEEN @FIVE_THIRTY_PM AND @SEVEN_THIRTY_PM)
        )
    GROUP BY
        sad.student_id,
        sad.studentName,
        sad.majorName,
        sad.degree_level,
        sad.generation
    HAVING totalTimeSpent > 0
    ORDER BY totalTimeSpent DESC
    LIMIT 10;
END;
*/

CREATE PROCEDURE IF NOT EXISTS CountDuration(
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    -- Time constants
    SET @SEVEN_AM = TIME('07:00:00');
    SET @ELEVEN_AM = TIME('11:00:00');
    SET @TWO_PM = TIME('14:00:00');
    SET @FIVE_PM = TIME('17:00:00');
    SET @FIVE_THIRTY_PM = TIME('17:30:00');
    SET @SEVEN_THIRTY_PM = TIME('19:30:00');

    SELECT
        student_id        AS studentId,
        student_name      AS studentName,
        major_name        AS major,
        degree_level      AS degree,
        generation,
        SUM(
            TIMESTAMPDIFF(
                    MINUTE,
                    entry_time,
                    exit_time
            )
        ) AS totalTimeSpent
    FROM vw_attend_details
    WHERE visitor_type = 'STUDENT'
        AND exit_time IS NOT NULL
        AND (startDate IS NULL OR attend_date >= startDate)
        AND (endDate IS NULL OR attend_date <= endDate)
        AND (
            TIME(exit_time) BETWEEN @SEVEN_AM AND @ELEVEN_AM
                OR TIME(exit_time) BETWEEN @TWO_PM AND @FIVE_PM
                OR TIME(exit_time) BETWEEN @FIVE_THIRTY_PM AND @SEVEN_THIRTY_PM
        )
    GROUP BY
        student_id,
        student_name,
        major_name,
        degree_level,
        generation
    HAVING totalTimeSpent > 0
    ORDER BY totalTimeSpent DESC
    LIMIT 10;
END;

CREATE PROCEDURE IF NOT EXISTS GetMostAttend(
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT
        major_name AS major,
        COUNT(*)   AS times,
        ROUND(
                COUNT(*) * 100.0 /
                (
                    SELECT COUNT(*)
                    FROM vw_attend_details
                    WHERE visitor_type = 'STUDENT'
                      AND (startDate IS NULL OR attend_date >= startDate)
                      AND (endDate IS NULL OR attend_date <= endDate)
                ),
                2
        ) AS percentage
    FROM vw_attend_details
    WHERE visitor_type = 'STUDENT'
      AND major_name IS NOT NULL
      AND (startDate IS NULL OR attend_date >= startDate)
      AND (endDate IS NULL OR attend_date <= endDate)
    GROUP BY major_name
    ORDER BY times DESC;
END;

CREATE PROCEDURE IF NOT EXISTS CountAttendByOpenTime(
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SET @SEVEN_AM = TIME('07:00:00');
    SET @ELEVEN_AM = TIME('11:00:00');
    SET @TWO_PM = TIME('14:00:00');
    SET @FIVE_PM = TIME('17:00:00');
    SET @FIVE_THIRTY_PM = TIME('17:30:00');
    SET @SEVEN_THIRTY_PM = TIME('19:30:00');

    SELECT
        COUNT(*) AS totalAttend,
        SUM(IF(gender = 'Female', 1, 0)) AS totalFemale,
        SUM(IF(TIME(entry_time) BETWEEN @SEVEN_AM AND @ELEVEN_AM, 1, 0)) AS totalMorningAttend,
        SUM(IF(TIME(entry_time) BETWEEN @TWO_PM AND @FIVE_PM, 1, 0)) AS totalAfternoonAttend,
        SUM(IF(TIME(entry_time) BETWEEN @FIVE_THIRTY_PM AND @SEVEN_THIRTY_PM, 1, 0)) AS totalEveningAttend
    FROM vw_attend_details
    WHERE visitor_type = 'STUDENT'
      AND (startDate IS NULL OR attend_date >= startDate)
      AND (endDate IS NULL OR attend_date <= endDate);
END;

CREATE PROCEDURE IF NOT EXISTS GetPurposeByMonth(
    IN majorName VARCHAR(100),
    IN startMonth VARCHAR(7),
    IN endMonth VARCHAR(7)
)
BEGIN
    DECLARE processStartDate DATE;
    DECLARE processEndDate DATE;

    SET processStartDate =
        IFNULL(
                STR_TO_DATE(CONCAT(startMonth, '-01'), '%Y-%m-%d'),
                STR_TO_DATE(CONCAT(YEAR(CURDATE()), '-01-01'), '%Y-%m-%d')
        );

    SET processEndDate =
        IFNULL(
                LAST_DAY(STR_TO_DATE(CONCAT(endMonth, '-01'), '%Y-%m-%d')),
                LAST_DAY(CURDATE())
        );

    SELECT
        DATE_FORMAT(attend_date, '%Y-%m') AS month,
        SUM(purpose LIKE '%Other%')       AS other,
        SUM(purpose LIKE '%Reading%')     AS reading,
        SUM(purpose LIKE '%Assignment%')  AS assignment,
        SUM(purpose LIKE '%Use PC%')       AS usePc
    FROM vw_attend_details
    WHERE visitor_type = 'STUDENT'
      AND attend_date BETWEEN processStartDate AND processEndDate
      AND (majorName IS NULL OR major_name = majorName)
    GROUP BY DATE_FORMAT(attend_date, '%Y-%m')
    ORDER BY month;
END;

CREATE PROCEDURE IF NOT EXISTS GetBorrowDataEachMajor(
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    DECLARE total_borrows INT DEFAULT 0;

    -- Get total borrows for percentage calculation
    SELECT COUNT(bb.borrow_id) INTO total_borrows
    FROM borrow_books bb
             JOIN students s ON bb.student_id = s.student_id
             JOIN majors m ON s.major_id = m.major_id
    WHERE bb.borrow_date BETWEEN startDate AND endDate;

    -- Return major borrow data with percentages
    SELECT
        m.major_name AS majorName,
        COUNT(bb.borrow_id) AS times,
        IF(total_borrows > 0, ROUND((COUNT(bb.borrow_id) * 100.0 / total_borrows), 2), 0) AS percentage
    FROM borrow_books bb
             JOIN students s ON bb.student_id = s.student_id
             JOIN majors m ON s.major_id = m.major_id
    WHERE bb.borrow_date BETWEEN startDate AND endDate
    GROUP BY m.major_id, m.major_name
    ORDER BY times DESC;
END;

CREATE PROCEDURE IF NOT EXISTS GetMostBorrowedBooks(
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    SELECT ROW_NUMBER() OVER (ORDER BY COUNT(b.book_id) DESC) AS ranking,
           b.book_title AS bookTitle,
           b.genre AS genre,
           COUNT(b.book_id) AS count
    FROM borrow_books bb
             INNER JOIN books b ON bb.book_id = b.book_id
    WHERE (
          (startDate IS NULL OR bb.borrow_date >= startDate) AND
          (endDate IS NULL OR bb.borrow_date <= endDate)
    )
    GROUP BY b.book_id, b.book_title, b.genre
    ORDER BY count DESC;
end;

CREATE PROCEDURE IF NOT EXISTS GetBorrowAndReturn(
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    -- Create a temporary table to hold all unique months from both borrow and return data
    CREATE TEMPORARY TABLE temp_months (
       month_year VARCHAR(7) PRIMARY KEY,
       year_month_date DATE
    );

    -- Insert unique months from borrow_date within the date range
    INSERT IGNORE INTO temp_months (month_year, year_month_date)
    SELECT
        DATE_FORMAT(borrow_date, '%Y-%m') as month_year,
        DATE(CONCAT(DATE_FORMAT(borrow_date, '%Y-%m'), '-01')) as year_month_date
    FROM borrow_books
    WHERE (startDate IS NULL OR borrow_date >= startDate)
      AND (endDate IS NULL OR borrow_date <= endDate);

    -- Insert unique months from give_back_date for returned books within the date range
    INSERT IGNORE INTO temp_months (month_year, year_month_date)
    SELECT
        DATE_FORMAT(give_back_date, '%Y-%m') as month_year,
        DATE(CONCAT(DATE_FORMAT(give_back_date, '%Y-%m'), '-01')) as year_month_date
    FROM borrow_books
    WHERE is_bring_back = true
      AND (startDate IS NULL OR give_back_date >= startDate)
      AND (endDate IS NULL OR give_back_date <= endDate);

    -- Main query to get borrow and return counts by month
    SELECT
        tm.month_year as month,
        COALESCE(borrow_counts.borrow_count, 0) as borrow,
        COALESCE(return_counts.return_count, 0) as returned
    FROM temp_months tm
             LEFT JOIN (
        -- Count borrows by month
        SELECT
            DATE_FORMAT(borrow_date, '%Y-%m') as month_year,
            COUNT(*) as borrow_count
        FROM borrow_books
        WHERE (startDate IS NULL OR borrow_date >= startDate)
          AND (endDate IS NULL OR borrow_date <= endDate)
        GROUP BY DATE_FORMAT(borrow_date, '%Y-%m')
    ) borrow_counts ON tm.month_year = borrow_counts.month_year
             LEFT JOIN (
        -- Count returns by month
        SELECT
            DATE_FORMAT(give_back_date, '%Y-%m') as month_year,
            COUNT(*) as return_count
        FROM borrow_books
        WHERE is_bring_back = true
          AND (startDate IS NULL OR give_back_date >= startDate)
          AND (endDate IS NULL OR give_back_date <= endDate)
        GROUP BY DATE_FORMAT(give_back_date, '%Y-%m')
    ) return_counts ON tm.month_year = return_counts.month_year
    ORDER BY tm.year_month_date;

    -- Clean up temporary table
    DROP TEMPORARY TABLE temp_months;
END;

