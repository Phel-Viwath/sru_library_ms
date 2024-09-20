/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.dto.entry

import sru.edu.sru_lib_management.core.domain.dto.attend.AttendDetail
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail

data class Entry(
    val cardEntry: List<CardEntry>,
    val attendDetail: List<StudentAttendDetail>
)
