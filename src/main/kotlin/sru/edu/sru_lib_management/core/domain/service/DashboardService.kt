package sru.edu.sru_lib_management.core.domain.service

import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.core.domain.dto.attend.StudentAttendDetail
import sru.edu.sru_lib_management.core.domain.dto.dashboard.CardData
import sru.edu.sru_lib_management.core.domain.dto.dashboard.Dashboard

@Service
interface DashboardService {
    suspend fun getDashboardData(): Dashboard
}