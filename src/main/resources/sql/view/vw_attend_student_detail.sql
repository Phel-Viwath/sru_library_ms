CREATE OR REPLACE VIEW vw_attend_student_detail AS
SELECT
    a.attend_id,
    a.attend_date,
    a.entry_time,
    a.exit_time,
    a.purpose,
    v.visitor_type,
    s.student_id AS visitor_id,
    s.student_name,
    s.gender,
    s.generation,
    m.major_name,
    d.degree_level
FROM attend a
         JOIN visitors v ON a.visitor_id = v.visitor_id
         JOIN students s ON v.student_id = s.student_id
         JOIN majors m ON s.major_id = m.major_id
         JOIN degree_level d ON s.degree_level_id = d.degree_level_id
WHERE v.visitor_type = 'STUDENT';

CREATE OR REPLACE VIEW vw_attend_staff_detail AS
SELECT
    a.attend_id,
    a.attend_date,
    a.entry_time,
    a.exit_time,
    a.purpose,
    v.visitor_type,
    st.sru_staff_id AS visitor_id,
    st.sru_staff_name,
    st.gender,
    NULL AS generation,
    NULL AS majorName,
    NULL AS degree_level
FROM attend a
         JOIN visitors v ON a.visitor_id = v.visitor_id
         JOIN sru_staff st ON v.sru_staff_id = st.sru_staff_id
WHERE v.visitor_type = 'SRU_STAFF';