/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sru.edu.sru_lib_management.core.domain.dto.CompareValue
import sru.edu.sru_lib_management.core.domain.dto.DonationDetailDto
import sru.edu.sru_lib_management.core.domain.model.BorrowBook
import sru.edu.sru_lib_management.core.domain.model.Donation
import sru.edu.sru_lib_management.core.domain.model.Donator
import sru.edu.sru_lib_management.core.domain.repository.crud.ICrudRepository
import java.awt.print.Book
import java.time.LocalDate

interface DonationRepository : ICrudRepository<Donator, Int>{
    suspend fun newDonation(donation: Donation): Donation
    suspend fun countCurrentAndPreviousDonation(date: LocalDate, period: Int): CompareValue
    suspend fun deleteDonation(donatorId: Int?, bookId: String?): Boolean
    fun getDonationDetail(): Flow<DonationDetailDto>



}