/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.dto.DonationDetailDto
import sru.edu.sru_lib_management.core.domain.model.Donator
import sru.edu.sru_lib_management.core.domain.model.DonationDetail
import java.time.LocalDate

@Service
interface DonationService {
    // donator
    suspend fun newDonation(donationDetails: List<DonationDetail>): CoreResult<List<Donator>>
    suspend fun updateDonation(donationDetail: DonationDetail): CoreResult<Donator>
    suspend fun deleteDonator(donatorId: Int): CoreResult<Boolean>
    suspend fun getDonator(donatorId: Int): CoreResult<Donator?>
    suspend fun getAllDonator(): Flow<Donator>

    // donation
    suspend fun deleteDonation(donatorId: Int?, bookId: String?): CoreResult<Boolean>

    // Donation detail
    suspend fun analyticDonation(date: LocalDate, period: Int): CoreResult<Analyze>
    fun getDonationDetail(): Flow<DonationDetailDto>

}