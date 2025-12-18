package sru.edu.sru_lib_management.core.domain.repository

import org.springframework.stereotype.Repository
import sru.edu.sru_lib_management.core.domain.model.Visitor

@Repository
interface VisitorRepository {
    suspend fun findByStudentId(studentId: Long): Visitor?
    suspend fun findByStaffId(staffId: String): Visitor?
    suspend fun save(visitor: Visitor): Visitor
}