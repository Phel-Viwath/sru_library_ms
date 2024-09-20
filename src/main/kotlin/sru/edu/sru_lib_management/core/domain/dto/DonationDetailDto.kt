/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class DonationDetailDto(
    val donatorId: Int?,
    val donatorName: String,
    val bookId: String,
    val bookTitle: String,
    val bookQuan: Int,
    val languageName: String,
    val collegeName: String,
    val author: String?,
    val publicationYear: Int?,
    val genre: String,
    @JsonFormat(pattern = "yyyy-MM-dd") val donateDate: LocalDate
)
