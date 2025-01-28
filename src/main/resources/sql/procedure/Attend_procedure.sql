/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

#USE sru_library;
#Count number of student that attend library custom by date
CREATE PROCEDURE if not exists CountTotalAttend()
BEGIN
    SELECT COUNT(*) as attendance_count from attend;
end;

##
# Get borrow book by custom time
Create PROCEDURE if not exists CountAttendByPeriod(in inputDate DATE, in period INT)
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

    SET previous_date = inputDate - interval 1 day;

    IF period = 1 Then
        # Select current date
        SELECT COUNT(*) INTO current_date_count
        FROM attend WHERE date = inputDate;
        # Select previous day
        SELECT COUNT(*) INTO previous_date_count
        FROM attend WHERE date = previous_date;
        # Return the results
        SELECT current_date_count as current_value,
               previous_date_count as previous_value;

    ELSEIF period = 7 Then
        #Select current week
        SELECT COUNT(*) INTO current_7_day_count
        FROM attend
        WHERE date BETWEEN inputDate - INTERVAL 6 day and inputDate;
        # Select previous week
        SELECT COUNT(*) INTO previous_7_day_count
        FROM attend
        WHERE date BETWEEN inputDate - INTERVAL 13 day and inputDate - INTERVAL 7 day;
        # Return the results
        SELECT current_7_day_count as current_value,
               previous_7_day_count as previous_value;

    ELSEIF period = 30 Then
        # Select count for current month
        SELECT COUNT(*) INTO current_month_count
        FROM attend
        WHERE YEAR(date) = YEAR(inputDate) AND MONTH(date) = MONTH(inputDate);
        # Select count for previous month
        SELECT COUNT(*) INTO previous_month_count
        FROM attend
        WHERE YEAR(date) = YEAR(inputDate - INTERVAL 1 MONTH) AND MONTH(date) = MONTH(inputDate - INTERVAL 1 MONTH);
        # Return Value
        SELECT current_month_count as current_value,
               previous_month_count as previous_value;

    ELSEIF period = 365 Then
        # Select count for current year
        SELECT COUNT(*) INTO current_year_count
        FROM attend
        WHERE date BETWEEN inputDate - INTERVAL 364 day and inputDate;
        # Select count for previous year
        SELECT COUNT(*) INTO previous_year_count
        FROM attend
        WHERE date between inputDate - INTERVAL ((365*2) - 1) day and inputDate - Interval 365 day ;
        # Return Value
        SELECT current_year_count as current_value,
               previous_year_count as previous_value;
    END if;
end;

## Count attend per week
CREATE PROCEDURE if not exists CountAttendPerWeek(IN Monday DATE, IN Sunday DATE)
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
        COALESCE(COUNT(a.date), 0) AS count
    FROM
        date_series ds
            LEFT JOIN
        attend a ON ds.date = a.date
    GROUP BY
        ds.date
    ORDER BY
        ds.date;

    DROP TEMPORARY TABLE date_series;
END;

# Get Attend detail
# Get attend with student info custom by date
Create Procedure if not exists GetCurrentAttendDetails()
BEGIN
    SELECT s.student_id as student_id,
           s.student_name as studentName,
           s.gender as gender,
           s.generation as generation,
           m.major_name as majorName,
           d.degree_level as degree_level,
           at.attend_id as attendId,
           at.entry_times as entryTimes,
           at.exiting_times as exitingTime,
           at.date as date,
           at.purpose as purpose
    From attend at
             INNER JOIN students s ON s.student_id = at.student_id
             INNER JOIN majors m ON s.major_id = m.major_id
            INNER JOIN degree_level d ON s.degree_level_id = d.degree_level_id
    WHERE at.date = CURDATE();
end;



# Get attend with student info custom by date
CREATE PROCEDURE GetAttendDetailByDate(IN date DATE)
BEGIN
    SELECT s.student_id as student_id,
           s.student_name as studentName,
           s.gender as gender,
           s.generation as generation,
           m.major_name as majorName,
           d.degree_level as degree_level,
           at.attend_id as attendId,
           at.entry_times as entryTimes,
           at.exiting_times as exitingTime,
           at.date as date,
           at.purpose as purpose
    From attend at
             INNER JOIN students s ON s.student_id = at.student_id
             INNER JOIN majors m ON s.major_id = m.major_id
            INNER JOIN degree_level d ON s.degree_level_id = d.degree_level_id
    WHERE at.date = date;
end;



# Get all attend with student info
CREATE PROCEDURE GetAllAttendDetail()
BEGIN
    SELECT s.student_id as student_id,
           s.student_name as studentName,
           s.gender as gender,
           s.generation as generation,
           m.major_name as majorName,
           d.degree_level as degree_level,
           at.attend_id as attendId,
           at.entry_times as entryTimes,
           at.exiting_times as exitingTime,
           at.date as date,
           at.purpose as purpose
    From attend at
            INNER JOIN students s ON s.student_id = at.student_id
            INNER JOIN majors m ON s.major_id = m.major_id
            INNER JOIN degree_level d ON s.degree_level_id = d.degree_level_id;
end;


# Get attend detail by id
CREATE PROCEDURE GetAttendDetailById(
    IN id BIGINT
)
BEGIN
    SELECT s.student_id as student_id,
           s.student_name as studentName,
           s.gender as gender,
           s.generation as generation,
           m.major_name as majorName,
           d.degree_level as degree_level,
           at.attend_id as attendId,
           at.entry_times as entryTimes,
           at.exiting_times as exitingTime,
           at.date as date,
           at.purpose as purpose
    From attend at
            INNER JOIN students s ON s.student_id = at.student_id
            INNER JOIN majors m ON s.major_id = m.major_id
            INNER JOIN degree_level d ON s.degree_level_id = d.degree_level_id
    where attend_id = id;
end;


# Count total major that attend library
CREATE PROCEDURE if not exists CountMajorAttendLib()
BEGIN
    SELECT
        m.major_name AS Major,
        COUNT(a.attend_id) AS Amount,
        ROUND(COUNT(a.attend_id) * 100.0 / (SELECT COUNT(*) FROM attend), 2) as Percentage
    FROM
        majors m INNER JOIN (attend a INNER JOIN students s ON s.student_id = a.student_id)
                            ON s.major_id = m.major_id
    GROUP BY
        m.major_name
    ORDER BY
        Amount DESC;
end;

## get attend custom time
CREATE PROCEDURE if not exists GetAttendByCustomTime(IN date DATE)
BEGIN
    SELECT * FROM attend where attend.date = date;
end;

# Get all attend with student info
CREATE PROCEDURE GetAttendDetailByPeriod(in date DATE, in entry_time TIME, in exiting_time TIME)
BEGIN
    SELECT s.student_id as student_id,
           s.student_name as studentName,
           s.gender as gender,
           s.generation as generation,
           m.major_name as majorName,
           d.degree_level as degree_level,
           at.attend_id as attendId,
           at.entry_times as entryTimes,
           at.exiting_times as exitingTime,
           at.date as date,
           at.purpose as purpose
    From attend at
            INNER JOIN students s ON s.student_id = at.student_id
            INNER JOIN majors m ON s.major_id = m.major_id
            INNER JOIN degree_level d ON s.degree_level_id = d.degree_level_id
    WHERE at.date = date and at.entry_times between entry_time and exiting_time;
end;


# Get all attend with student info
CREATE PROCEDURE GetCustomAttendDetail(
    IN p_startDate DATE,
    IN p_endDate DATE
)
BEGIN
    SELECT s.student_id as student_id,
           s.student_name as studentName,
           s.gender as gender,
           s.generation as generation,
           m.major_name as majorName,
           d.degree_level as degree_level,
           at.attend_id as attendId,
           at.entry_times as entryTimes,
           at.exiting_times as exitingTime,
           at.date as date,
           at.purpose as purpose
    From attend at
        INNER JOIN students s ON s.student_id = at.student_id
        INNER JOIN majors m ON s.major_id = m.major_id
        INNER JOIN degree_level d ON s.degree_level_id = d.degree_level_id
    where (p_startDate IS NULL OR at.date >= p_startDate) AND (p_endDate IS NULL OR at.date <= p_endDate);
end;

# Minute count
CREATE PROCEDURE CountDurationSpent()
BEGIN
    SELECT a.student_id as student_id,
           s.student_name as student_name,
           a.date as date,
           SUM(TIMESTAMPDIFF(MINUTE , a.entry_times, a.exiting_times)) as total_minute_spent
    FROM attend a inner join students s
    WHERE exiting_times is not null
    and
        (TIME(exiting_times) between '07:00:00' and '11:00:00') or
        (TIME(exiting_times) between '14:00:00' and '17:00:00') or
        (TIME(exiting_times) between '17:30:00' and '19:30:00')
    group by a.student_id, s.student_name, a.date ;
end;

# Get all attend with student info
CREATE PROCEDURE GetAttendDetailByPeriod(in date DATE, in entry_time TIME, in exiting_time TIME)
BEGIN
    SELECT s.student_id as student_id,
           s.student_name as studentName,
           s.gender as gender,
           s.generation as generation,
           m.major_name as majorName,
           at.attend_id as attendId,
           at.entry_times as entryTimes,
           at.exiting_times as exitingTime,
           at.date as date,
           at.purpose as purpose
    From attend at
             INNER JOIN students s ON s.student_id = at.student_id
             INNER JOIN majors m ON s.major_id = m.major_id
    WHERE at.date = date and at.entry_times between entry_time and exiting_time;
end;



CREATE PROCEDURE GetAttendDetail(In date DATE)
BEGIN
    SELECT
        COALESCE(s.student_id, st.sru_staff_id) as id,
        COALESCE(s.student_name, st.sru_staff_name) AS name,
        a.entry_times AS entry_time,
        a.exiting_times AS exiting_time,
        a.purpose as purpose
    FROM
        attend a
            LEFT JOIN
        students s ON a.student_id = s.student_id
            LEFT JOIN
        sru_staff st ON a.sru_staff_id = st.sru_staff_id
     WHERE a.date = date;
end;

