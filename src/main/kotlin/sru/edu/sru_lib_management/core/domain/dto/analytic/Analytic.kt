/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.analytic

import sru.edu.sru_lib_management.core.domain.dto.DurationSpent
import sru.edu.sru_lib_management.core.domain.dto.PurposeByMonthDto
import sru.edu.sru_lib_management.core.domain.dto.PurposeDto

data class Analytic (
    val bookIncome: List<BookIncome>,
    val purposeCount: List<PurposeDto>,
    val totalBook: TotalBook,
    val bookEachCollege: List<BookEachCollege>,
    val timeSpent: List<DurationSpent>,
    val mostMajorBorrows: List<MajorAttendBorrowed>,
    val mostBorrowBook: List<MostBorrow>,
    val mostMajorAttend: List<MajorAttendBorrowed>,
    val studentEntryByTime: TotalStudentAttendByTime,
    val getPurpose : List<PurposeByMonthDto>,
    val borrowAndReturned: List<BorrowReturn>
)