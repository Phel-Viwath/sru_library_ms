package sru.edu.sru_lib_management.core.data.query

object AttendQuery {
    const val SAVE_ATTEND_QUERY = """
            INSERT INTO attend(visitor_id, entry_time, exit_time, attend_date, purpose)
            VALUES(:visitorId, :entryTimes, :exitTimes, :attendDate, :purpose);
        """
    const val UPDATE_ATTEND_QUERY = """
            UPDATE attend set entry_time = :entryTimes,
            exit_time = :exitTimes, attend_date = :attendDate, purpose = :purpose
            WHERE attend_id = :attendId;
        """
    const val DELETE_ATTEND_QUERY = "DELETE FROM attend WHERE attend_id = :attendId;"
    const val GET_ATTEND_QUERY = "SELECT * FROM attend WHERE attend_id = :attendId;"

    const val GET_ALL_ATTEND_QUERY = "SELECT * FROM attend;"

    const val UPDATE_EXIT_TIME = "UPDATE attend SET exit_time = :exitTimes WHERE attend_id = :attendId AND attend_date = :date;"

    const val GET_ALL_STAFF_ATTEND = """
            SELECT
                attend_id,
                sru_staff_id   AS staff_id,
                sru_staff_name AS staff_name,
                gender,
                entry_time,
                exit_time,
                purpose,
                attend_date
            FROM vw_attend_details
            WHERE visitor_type = 'SRU_STAFF'
        """

    const val GET_VISITOR_BY_ATTEND_ID = """
            SELECT 
                a.visitor_id,
                v.student_id,
                v.sru_staff_id
            FROM attend a
            JOIN visitors v ON a.visitor_id = v.visitor_id
            WHERE a.attend_id = :attendId
        """
    const val UPDATE_EXIT_TIME_BY_VISITOR = """
        UPDATE attend
        SET exit_time = :exitTime
        WHERE visitor_id = :visitorId
          AND attend_date = :date
          AND exit_time IS NULL
    """
}