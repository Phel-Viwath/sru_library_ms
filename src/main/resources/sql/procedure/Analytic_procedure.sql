
CREATE PROCEDURE IF NOT EXISTS PurposeCount(
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_major_name VARCHAR(100)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    -- Validate input parameters
    IF p_start_date IS NULL OR p_end_date IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Start date and end date cannot be null';
    END IF;

    IF p_start_date > p_end_date THEN
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
                     ELSE 'Other'
                     END as purpose_category,
                 COUNT(*) as purpose_count
             FROM attend a
                      LEFT JOIN students s ON a.student_id = s.student_id
                      LEFT JOIN majors m ON s.major_id = m.major_id
             WHERE
                 a.date BETWEEN p_start_date AND p_end_date
               AND (
                 p_major_name IS NULL
                     OR p_major_name = ''
                     OR m.major_name = p_major_name
                     OR a.sru_staff_id IS NOT NULL  -- Include staff entries when major filter is applied
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

END;

CREATE PROCEDURE IF NOT EXISTS GetBookForEachCollege(
    IN startDate DATE,
    IN endDate DATE
)BEGIN

    SELECT
        c.college_name,
        SUM(b.bookQuan) AS total_book
    FROM books b
             JOIN colleges c ON b.college_id = c.college_id
             LEFT JOIN donation d ON b.book_id = d.book_id
    WHERE (
        (b.received_date IS NOT NULL AND b.received_date BETWEEN startDate AND endDate)
            OR
        (d.donate_date IS NOT NULL AND d.donate_date BETWEEN startDate AND endDate)
            OR
        (startDate IS NULL AND endDate IS NULL)
    )AND b.isActive = TRUE
    GROUP BY c.college_name ORDER BY c.college_name;
end;

CREATE PROCEDURE GetBookLanguage()
BEGIN
    SELECT
        l.language_name AS languageName,
        SUM(b.bookQuan) AS totalBookPerLanguage
    FROM books b
             JOIN language l ON b.language_id = l.language_id
    WHERE b.isActive = true
    GROUP BY b.language_id;
end;

CREATE PROCEDURE GetBookInCome(
    IN startDate DATE,
    IN endDate DATE
)
BEGIN
    -- Exit handler for SQL errors
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            -- Optional: Log error
            RESIGNAL;
        END;

    -- Create temp table with UNIQUE key on month
    CREATE TEMPORARY TABLE temp_book_income (
        month VARCHAR(7) PRIMARY KEY,
        donation INT DEFAULT 0,
        university_funding INT DEFAULT 0
    );

    -- Insert university funding data
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

    -- Insert or update donation data
    INSERT INTO temp_book_income (month, donation)
    SELECT
        DATE_FORMAT(donate_date, '%Y-%m') AS month,
        COUNT(*) AS donation
    FROM donation
    WHERE (startDate IS NULL OR donate_date >= startDate)
      AND (endDate IS NULL OR donate_date <= endDate)
    GROUP BY DATE_FORMAT(donate_date, '%Y-%m')
    ON DUPLICATE KEY UPDATE donation = VALUES(donation);

    -- Final combined result
    SELECT
        month,
        COALESCE(donation, 0) AS donation,
        COALESCE(university_funding, 0) AS university_funding
    FROM temp_book_income
    ORDER BY month;
end;

CREATE PROCEDURE CountDuration(
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
                 a.entry_times AS entryTimes,
                 a.exiting_times AS exitingTime
             FROM attend a
                      INNER JOIN students s ON a.student_id = s.student_id
                      INNER JOIN majors m ON s.major_id = m.major_id
                      INNER JOIN degree_level dl ON s.degree_level_id = dl.degree_level_id
             WHERE a.student_id IS NOT NULL
               AND (startDate IS NULL OR a.date >= startDate)
               AND (endDate IS NULL OR a.date <= endDate)
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
    ORDER BY totalTimeSpent DESC;
END;

CREATE PROCEDURE GetMostAttend(
    IN startDate DATE,  -- Start date or NULL
    IN endDate DATE     -- End date or NULL
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    -- Calculate major attendance with counts and percentages
    SELECT
        m.major_name AS major,
        COUNT(*) AS times,
        ROUND(
                (COUNT(*) * 100.0) / (
                    SELECT COUNT(*)
                    FROM attend a2
                             INNER JOIN students s2 ON a2.student_id = s2.student_id
                    WHERE a2.student_id IS NOT NULL
                      AND (startDate IS NULL OR a2.date >= startDate)
                      AND (endDate IS NULL OR a2.date <= endDate)
                ),
                2
        ) AS percentage
    FROM attend a
             INNER JOIN students s ON a.student_id = s.student_id
             INNER JOIN majors m ON s.major_id = m.major_id
    WHERE a.student_id IS NOT NULL
      AND (startDate IS NULL OR a.date >= startDate)
      AND (endDate IS NULL OR a.date <= endDate)
    GROUP BY m.major_id, m.major_name
    ORDER BY times DESC;

END;

CREATE PROCEDURE CountAttendByOpenTime(
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

    -- Calculate attendance statistics by time periods
    SELECT
        COUNT(*)                           AS totalAttend,
        SUM(IF(s.gender = 'Female', 1, 0)) AS totalFemale,
        SUM(
                IF(TIME(a.entry_times) BETWEEN @SEVEN_AM AND @ELEVEN_AM, 1, 0)
        )                                  AS totalMorningAttend,
        SUM(
                IF(TIME(a.entry_times) BETWEEN @TWO_PM AND @FIVE_PM, 1, 0)
        )                                  AS totalAfternoonAttend,
        SUM(
                IF(TIME(a.entry_times) BETWEEN @FIVE_THIRTY_PM AND @SEVEN_THIRTY_PM, 1, 0)
        ) AS totalEveningAttend
    FROM attend a
             INNER JOIN students s ON a.student_id = s.student_id
    WHERE a.student_id IS NOT NULL
      AND (startDate IS NULL OR a.date >= startDate)
      AND (endDate IS NULL OR a.date <= endDate);

END;

CREATE PROCEDURE GetPurposeByMonth(
    IN majorName VARCHAR(100),  -- Major name filter or NULL
    IN startMonth DATE,         -- Start month (any date in the month) or NULL
    IN endMonth DATE            -- End month (any date in the month) or NULL
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    -- Set default date range if not provided (current year from January to current month)
    SET @defaultStartMonth = CONCAT(YEAR(CURDATE()), '-01-01');
    SET @defaultEndMonth = LAST_DAY(CURDATE());

    -- Use provided dates or defaults
    SET @processStartMonth = COALESCE(startMonth, @defaultStartMonth);
    SET @processEndMonth = COALESCE(endMonth, @defaultEndMonth);

    -- Get purpose counts grouped by month
    SELECT
        DATE_FORMAT(a.date, '%Y-%m') AS month,
        SUM(
                IF(FIND_IN_SET('Other', REPLACE(a.purpose, ', ', ',')) > 0, 1, 0)
        ) AS other,
        SUM(
                IF(FIND_IN_SET('Reading', REPLACE(a.purpose, ', ', ',')) > 0, 1, 0)
        ) AS reading,
        SUM(
                IF(FIND_IN_SET('Assignment', REPLACE(a.purpose, ', ', ',')) > 0, 1, 0)
        ) AS assignment,
        SUM(
                IF(FIND_IN_SET('Use PC', REPLACE(a.purpose, ', ', ',')) > 0, 1, 0)
        ) AS usePc
    FROM attend a
             INNER JOIN students s ON a.student_id = s.student_id
             INNER JOIN majors m ON s.major_id = m.major_id
    WHERE a.student_id IS NOT NULL
      AND a.date >= DATE_FORMAT(@processStartMonth, '%Y-%m-01')
      AND a.date <= LAST_DAY(@processEndMonth)
      AND (majorName IS NULL OR m.major_name = majorName)
    GROUP BY DATE_FORMAT(a.date, '%Y-%m')
    ORDER BY month;

END;



