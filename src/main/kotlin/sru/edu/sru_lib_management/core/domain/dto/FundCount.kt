/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto

import java.time.YearMonth

data class FundCount(
    val month: YearMonth,
    val bookCount: Int
)
