CREATE OR REPLACE VIEW vw_visitor_attend_detail AS
SELECT
    a.attend_id AS attendId,
    v.visitor_id AS visitorId,
    COALESCE(s.student_name, st.sru_staff_name) AS visitorName,
    v.visitor_type AS visitorType,
    a.entry_time AS entryTimes,
    a.exit_time AS exitTimes,
    a.purpose AS purpose,
    a.attend_date AS attendDate
FROM attend a
         JOIN visitors v ON a.visitor_id = v.visitor_id
         LEFT JOIN students s ON v.student_id = s.student_id
         LEFT JOIN sru_staff st ON v.sru_staff_id = st.sru_staff_id;
