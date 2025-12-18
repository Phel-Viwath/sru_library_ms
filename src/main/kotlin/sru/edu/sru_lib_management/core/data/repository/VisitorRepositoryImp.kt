package sru.edu.sru_lib_management.core.data.repository

import io.r2dbc.spi.Row
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.core.domain.model.Visitor
import sru.edu.sru_lib_management.core.domain.model.VisitorType
import sru.edu.sru_lib_management.core.domain.repository.VisitorRepository

@Component
class VisitorRepositoryImp(
    private val client: DatabaseClient
) : VisitorRepository {

    companion object {
        private const val FIND_BY_STUDENT_ID = """
        SELECT visitor_id, visitor_type, student_id, sru_staff_id
        FROM visitors
        WHERE student_id = :studentId
    """

        private const val FIND_BY_STAFF_ID = """
        SELECT visitor_id, visitor_type, student_id, sru_staff_id
        FROM visitors
        WHERE sru_staff_id = :staffId
    """

        private const val SAVE_VISITOR = """
        INSERT INTO visitors (visitor_type, student_id, sru_staff_id)
        VALUES (:visitorType, :studentId, :sruStaffId)
    """
    }

    override suspend fun findByStudentId(studentId: Long): Visitor? {
        return client.sql(FIND_BY_STUDENT_ID)
            .bind("studentId", studentId)
            .map { row: Row, _ -> row.rowMapping() }
            .awaitSingleOrNull()
    }

    override suspend fun findByStaffId(staffId: String): Visitor? {
        return client.sql(FIND_BY_STAFF_ID)
            .bind("staffId", staffId)
            .map { row: Row, _ -> row.rowMapping() }
            .awaitSingleOrNull()
    }

    override suspend fun save(visitor: Visitor): Visitor {
        var visitorId = 0L

        val spec = client.sql(SAVE_VISITOR)
            .filter { statement, next ->
                next.execute(statement.returnGeneratedValues("visitor_id"))
            }
            .bind("visitorType", visitor.visitorType.name)

        // bind nullable studentId
        val specWithStudent = if (visitor.studentId != null) {
            spec.bind("studentId", visitor.studentId)
        } else {
            spec.bindNull("studentId", java.lang.Long::class.java)
        }

        // bind nullable staffId
        val finalSpec = if (visitor.sruStaffId != null) {
            specWithStudent.bind("sruStaffId", visitor.sruStaffId)
        } else {
            specWithStudent.bindNull("sruStaffId", String::class.java)
        }

        val result = finalSpec.fetch().awaitOneOrNull()

        if (result != null && result.containsKey("visitor_id")) {
            visitorId = (result["visitor_id"] as Number).toLong()
        }

        return visitor.copy(visitorId = visitorId)
    }

    private fun Row.rowMapping(): Visitor = Visitor(
        visitorId = this.get("visitor_id", java.lang.Long::class.java)?.toLong(),
        visitorType = VisitorType.valueOf(this.get("visitor_type", String::class.java)!!),
        studentId = this.get("student_id", java.lang.Long::class.java)?.toLong(),
        sruStaffId = this.get("sru_staff_id", String::class.java)
    )
}