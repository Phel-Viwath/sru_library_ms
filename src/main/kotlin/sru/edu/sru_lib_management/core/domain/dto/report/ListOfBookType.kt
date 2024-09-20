/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.report

data class ListOfBookType (
    val bookType: String,
    val bookEachYear: Map<String, BookLanguageData>
)