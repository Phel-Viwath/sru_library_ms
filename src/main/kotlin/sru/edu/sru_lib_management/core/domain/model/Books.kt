/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Books (
    val bookId: String,
    val bookTitle: String,
    val bookQuan: Int,
    val languageId: String,
    val collegeId: String,
    val author: String?,
    val publicationYear: Int?,
    val genre: String,
    val receiveDate: LocalDate?,
    val isActive: Boolean,
    @field:JsonFormat(pattern = "dd:MM:yyyy")
    val inactiveDate: LocalDate?
)
