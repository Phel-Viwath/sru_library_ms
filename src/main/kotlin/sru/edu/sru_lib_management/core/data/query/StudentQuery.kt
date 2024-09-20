/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.query

object StudentQuery {
    const val SAVE_STUDENT_QUERY =
        "Insert into students (student_id, student_name, gender, date_of_birth, degree_level_id, major_id, generation) " +
                "VALUES (:studentId, :studentName, :gender, :dateOfBirth, :degreeLevelId, :majorId, :generation);"

    const val UPDATE_STUDENT_QUERY =
        "UPDATE students SET student_name = :studentName, gender = :gender, date_of_birth = :dateOfBirth, " +
                "degree_level_id = :degreeLevelId, major_id = :majorId, generation = :generation WHERE student_id = :studentId;"

    const val DELETE_STUDENT_QUERY = "DELETE FROM students WHERE student_id = :studentId;"


    const val GET_STUDENTS_QUERY = "Select * from students"

    const val GET_STUDENT_QUERY = "Select * from students where student_id = :studentId"

    const val GET_STUDENT_DETAIL_QUERY = """
        SELECT 
           s.student_id AS studentId,
           s.student_name AS studentName,
           s.gender AS gender,
           s.date_of_birth AS dateOfBirth,
           d.degree_level AS degreeLevel,
           m.major_name AS majorName,
           s.generation AS generation
        FROM 
           students s
           JOIN majors m ON s.major_id = m.major_id
           JOIN degree_level d ON s.degree_level_id = d.degree_level_id
        Where s.student_id = :studentId;
    """

    const val GET_STUDENTS_DETAIL_QUERY = """
       SELECT 
           s.student_id AS studentId,
           s.student_name AS studentName,
           s.gender AS gender,
           s.date_of_birth AS dateOfBirth,
           d.degree_level AS degreeLevel,
           m.major_name AS majorName,
           s.generation AS generation
       FROM 
           students s
           JOIN majors m ON s.major_id = m.major_id
           JOIN degree_level d ON s.degree_level_id = d.degree_level_id;
    """
}
