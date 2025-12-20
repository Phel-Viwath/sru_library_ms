/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

#USE sru_library;
#Count the number of students that attend library custom by date

CREATE PROCEDURE if not exists CountTotalAttend()
BEGIN
    SELECT COUNT(*) as attendance_count from attend;
end;

##
# Get a borrow book by custom time

Create PROCEDURE if not exists CountAttendByPeriod(
    in inputDate DATE, in period INT
)BEGIN
    DECLARE current_date_count INT;
    DECLARE previous_date DATE;
    DECLARE previous_date_count INT;
    DECLARE current_7_day_count INT;
    DECLARE previous_7_day_count INT;
    DECLARE current_month_count INT;
    DECLARE previous_month_count INT;
    DECLARE current_year_count INT;
    DECLARE previous_year_count INT;

    SET previous_date = inputDate - interval 1 day;

    IF period = 1 Then
        # Select current date
        SELECT COUNT(*) INTO current_date_count
        FROM attend WHERE attend_date = inputDate;
        # Select the previous day
        SELECT COUNT(*) INTO previous_date_count
        FROM attend WHERE attend_date = previous_date;
        # Return the results
        SELECT current_date_count as current_value,
               previous_date_count as previous_value;

    ELSEIF period = 7 Then
        #Select current week
        SELECT COUNT(*) INTO current_7_day_count
        FROM attend
        WHERE attend.attend_date BETWEEN inputDate - INTERVAL 6 day and inputDate;
        # Select previous week
        SELECT COUNT(*) INTO previous_7_day_count
        FROM attend
        WHERE attend_date BETWEEN inputDate - INTERVAL 13 day and inputDate - INTERVAL 7 day;
        # Return the results
        SELECT current_7_day_count as current_value,
               previous_7_day_count as previous_value;

    ELSEIF period = 30 Then
        # Select count for current month
        SELECT COUNT(*) INTO current_month_count
        FROM attend
        WHERE YEAR(attend_date) = YEAR(inputDate) AND MONTH(attend_date) = MONTH(inputDate);
        # Select count for previous month
        SELECT COUNT(*) INTO previous_month_count
        FROM attend
        WHERE YEAR(attend_date) = YEAR(inputDate - INTERVAL 1 MONTH) AND MONTH(attend_date) = MONTH(inputDate - INTERVAL 1 MONTH);
        # Return Value
        SELECT current_month_count as current_value,
               previous_month_count as previous_value;

    ELSEIF period = 365 Then
        # Select count for current year
        SELECT COUNT(*) INTO current_year_count
        FROM attend
        WHERE attend_date BETWEEN inputDate - INTERVAL 364 day and inputDate;
        # Select count for the previous year
        SELECT COUNT(*) INTO previous_year_count
        FROM attend
        WHERE attend_date between inputDate - INTERVAL ((365*2) - 1) day and inputDate - Interval 365 day ;
        # Return Value
        SELECT current_year_count as current_value,
               previous_year_count as previous_value;
    END if;
end;

## Count attend per week

CREATE PROCEDURE if not exists CountAttendPerWeek(
    IN Monday DATE, IN Sunday DATE
)
BEGIN

    CREATE TEMPORARY TABLE date_series (
         date DATE
    );

    SET @startDate = Monday;
    WHILE @startDate <= Sunday DO
            INSERT INTO date_series VALUES (@startDate);
            SET @startDate = DATE_ADD(@startDate, INTERVAL 1 DAY);
        END WHILE;

    SELECT
        ds.date,
        DAYNAME(ds.date) AS day_name,
        COALESCE(COUNT(a.attend_date), 0) AS count
    FROM
        date_series ds
            LEFT JOIN
        attend a ON ds.date = a.attend_date
    GROUP BY
        ds.date
    ORDER BY
        ds.date;

    DROP TEMPORARY TABLE date_series;
END;

# Get Attend detail
# Get attend with student info custom by date

CREATE PROCEDURE IF NOT EXISTS GetCurrentAttendDetails()
BEGIN
    SELECT
        a.attend_id AS attendId,
        v.visitor_type,
        s.student_id,
        s.student_name AS studentName,
        st.sru_staff_id,
        st.sru_staff_name AS staffName,
        s.gender,
        s.generation,
        m.major_name AS majorName,
        d.degree_level,
        a.entry_time AS entryTimes,
        a.exit_time AS exitingTime,
        a.attend_date AS date,
        a.purpose
    FROM attend a
             INNER JOIN visitors v ON a.visitor_id = v.visitor_id
             LEFT JOIN students s ON v.student_id = s.student_id
             LEFT JOIN majors m ON s.major_id = m.major_id
             LEFT JOIN degree_level d ON s.degree_level_id = d.degree_level_id
             LEFT JOIN sru_staff st ON v.sru_staff_id = st.sru_staff_id
    WHERE a.attend_date = CURDATE();
END;

# Get attending with student info custom by date

CREATE PROCEDURE GetAttendDetailByDate(
    IN p_date DATE,
    IN p_visitor_type ENUM('STUDENT','SRU_STAFF')
)
BEGIN
    IF p_visitor_type = 'STUDENT' THEN
        SELECT * FROM vw_attend_student_detail
        WHERE attend_date = p_date;
    ELSE
        SELECT * FROM vw_attend_staff_detail
        WHERE attend_date = p_date;
    END IF;
END;

# Get all attending with student info

CREATE PROCEDURE GetAllAttendDetail(
    IN p_visitor_type ENUM('STUDENT','SRU_STAFF')
)
BEGIN
    IF p_visitor_type = 'STUDENT' THEN
        SELECT * FROM vw_attend_student_detail;
    ELSE
        SELECT * FROM vw_attend_staff_detail;
    END IF;
END;


/* Get attend detail by id*/

CREATE PROCEDURE GetAttendDetailById(
    IN p_attend_id BIGINT,
    IN p_visitor_type ENUM('STUDENT','SRU_STAFF')
)
BEGIN
    IF p_visitor_type = 'STUDENT' THEN
        SELECT * FROM vw_attend_student_detail
        WHERE attend_id = p_attend_id;
    ELSE
        SELECT * FROM vw_attend_staff_detail
        WHERE attend_id = p_attend_id;
    END IF;
END;

# Count total major that attends a library

CREATE PROCEDURE CountMajorAttendLib()
BEGIN
    SELECT
        m.major_name AS Major,
        COUNT(a.attend_id) AS Amount,
        ROUND(
                COUNT(a.attend_id) * 100.0 /
                (SELECT COUNT(*)
                 FROM attend a2
                          JOIN visitors v2 ON a2.visitor_id = v2.visitor_id
                 WHERE v2.visitor_type = 'STUDENT'),
                2
        ) AS Percentage
    FROM attend a
             JOIN visitors v ON a.visitor_id = v.visitor_id
             JOIN students s ON v.student_id = s.student_id
             JOIN majors m ON s.major_id = m.major_id
    GROUP BY m.major_name
    ORDER BY Amount DESC;
END;

## get to attend custom time

CREATE PROCEDURE if not exists GetAttendByCustomTime(IN date DATE)
BEGIN
    SELECT * FROM attend where attend.attend_date = date;
end;

# Get all attending with student info

CREATE PROCEDURE GetAttendDetailByPeriod(
    IN p_date DATE,
    IN p_entry_time TIME,
    IN p_exiting_time TIME,
    IN p_visitor_type ENUM('STUDENT','SRU_STAFF')
)
BEGIN
    IF p_visitor_type = 'STUDENT' THEN
        SELECT *
        FROM vw_attend_student_detail
        WHERE attend_date = p_date
          AND entry_time BETWEEN p_entry_time AND p_exiting_time;
    ELSE
        SELECT *
        FROM vw_attend_staff_detail
        WHERE attend_date = p_date
          AND entry_time BETWEEN p_entry_time AND p_exiting_time;
    END IF;
END;


# Get all attending with student info

CREATE PROCEDURE GetCustomAttendDetail(
    IN p_startDate DATE,
    IN p_endDate DATE,
    IN p_visitor_type ENUM('STUDENT','SRU_STAFF')
)
BEGIN
    IF p_visitor_type = 'STUDENT' THEN
        SELECT *
        FROM vw_attend_student_detail
        WHERE (p_startDate IS NULL OR attend_date >= p_startDate)
          AND (p_endDate IS NULL OR attend_date <= p_endDate);
    ELSE
        SELECT *
        FROM vw_attend_staff_detail
        WHERE (p_startDate IS NULL OR attend_date >= p_startDate)
          AND (p_endDate IS NULL OR attend_date <= p_endDate);
    END IF;
END;

# Minute count

CREATE PROCEDURE IF NOT EXISTS CountDurationSpent()
BEGIN
    SELECT
        s.student_id AS student_id,
        s.student_name AS student_name,
        a.attend_date AS date,
        SUM(
                TIMESTAMPDIFF(MINUTE, a.entry_time, a.exit_time)
        ) AS total_minute_spent
    FROM attend a
             INNER JOIN visitors v ON a.visitor_id = v.visitor_id
             INNER JOIN students s ON v.student_id = s.student_id
    WHERE a.exit_time IS NOT NULL
      AND (
        TIME(a.exit_time) BETWEEN '07:00:00' AND '11:00:00'
            OR TIME(a.exit_time) BETWEEN '14:00:00' AND '17:00:00'
            OR TIME(a.exit_time) BETWEEN '17:30:00' AND '19:30:00'
        )
    GROUP BY
        s.student_id,
        s.student_name,
        a.attend_date;
END;

# Get all attending with student info

CREATE PROCEDURE IF NOT EXISTS GetAttendDetail(IN p_date DATE)
BEGIN
    SELECT
        COALESCE(s.student_id, st.sru_staff_id) AS id,
        COALESCE(s.student_name, st.sru_staff_name) AS name,
        a.entry_time AS entry_time,
        a.exit_time AS exiting_time,
        a.purpose AS purpose
    FROM attend a
             INNER JOIN visitors v ON a.visitor_id = v.visitor_id
             LEFT JOIN students s ON v.student_id = s.student_id
             LEFT JOIN sru_staff st ON v.sru_staff_id = st.sru_staff_id
    WHERE a.attend_date = p_date;
END;

CREATE PROCEDURE GetVisitorDetail(
    IN p_date DATE,
    IN p_visitor_type ENUM('STUDENT','SRU_STAFF')
)
BEGIN
    SELECT
        attendId,
        visitorId,
        visitorName,
        visitorType,
        entryTimes,
        exitTimes,
        purpose,
        attendDate
    FROM vw_visitor_attend_detail
    WHERE (p_date IS NULL OR attendDate = p_date)
      AND (p_visitor_type IS NULL OR visitorType = p_visitor_type)
    ORDER BY attendDate DESC, entryTimes;
END;