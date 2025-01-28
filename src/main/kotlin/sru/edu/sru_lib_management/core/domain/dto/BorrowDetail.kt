package sru.edu.sru_lib_management.core.domain.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class BorrowDetail(
    val borrowId: Long?,
    val bookId: String,
    val bookTitle: String,
    val bookQuan: Int,
    val studentId: Long,
    val studentName: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val borrowDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val giveBackDate: LocalDate,
    val isBringBack: Boolean,
    val isExtend: Boolean
)