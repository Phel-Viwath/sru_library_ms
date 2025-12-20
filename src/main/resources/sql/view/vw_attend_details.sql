CREATE OR REPLACE VIEW vw_attend_details AS
SELECT
    a.attend_id,
    v.visitor_id,
    a.entry_time,
    a.exit_time,
    a.attend_date,
    a.purpose,
    v.visitor_type,
    s.student_id,
    s.student_name,
    s.gender,
    s.generation,
    m.major_name,
    d.degree_level,
    st.sru_staff_id,
    st.sru_staff_name,
    st.position
FROM attend a
         JOIN visitors v ON a.visitor_id = v.visitor_id
         LEFT JOIN students s ON v.student_id = s.student_id
         LEFT JOIN majors m ON s.major_id = m.major_id
         LEFT JOIN degree_level d ON s.degree_level_id = d.degree_level_id
         LEFT JOIN sru_staff st ON v.sru_staff_id = st.sru_staff_id;