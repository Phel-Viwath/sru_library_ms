/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.report

import kotlinx.coroutines.flow.Flow
import sru.edu.sru_lib_management.core.domain.dto.DonationDetailDto
import sru.edu.sru_lib_management.core.domain.dto.analytic.BookEachCollege
import sru.edu.sru_lib_management.core.domain.mapper.dto.StaffDto

data class Report(
    val libraryStaff: List<StaffDto>,
    val totalBookInLibrary: Map<String, Int>,
    val bookEachCollege: List<BookEachCollege>,
    val staffMonthlyEntry: List<MonthlyEntry>,
    val studentMonthlyEntry: List<MonthlyEntry>,
    val listOfDonation: List<DonationDetailDto>
)
