/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.analytic

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.YearMonth

data class BorrowReturn(
    @JsonFormat(pattern = "yyyy-MM") val month: YearMonth,
    val borrow: Int,
    val returned: Int
)
