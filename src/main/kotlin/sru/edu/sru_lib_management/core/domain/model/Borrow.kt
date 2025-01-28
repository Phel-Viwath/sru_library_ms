/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

class Borrow (
    val borrowId: Long?,
    val bookId: String,
    val bookQuan: Int,
    val studentId: Long,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val borrowDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val giveBackDate: LocalDate,
    val isBringBack: Boolean,
    val isExtend: Boolean
)
