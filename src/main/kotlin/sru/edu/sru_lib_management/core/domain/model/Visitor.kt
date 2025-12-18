package sru.edu.sru_lib_management.core.domain.model

data class Visitor(
    val visitorId: Long?,
    val visitorType: VisitorType,
    val studentId: Long?,
    val sruStaffId: String?
)