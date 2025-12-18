/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.domain.service.implementation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.common.APIException
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.dto.Analyze
import sru.edu.sru_lib_management.core.domain.dto.DonationDetailDto
import sru.edu.sru_lib_management.core.domain.model.Books
import sru.edu.sru_lib_management.core.domain.model.Donation
import sru.edu.sru_lib_management.core.domain.model.DonationDetail
import sru.edu.sru_lib_management.core.domain.model.Donator
import sru.edu.sru_lib_management.core.domain.repository.BookRepository
import sru.edu.sru_lib_management.core.domain.repository.CollegeRepository
import sru.edu.sru_lib_management.core.domain.repository.DonationRepository
import sru.edu.sru_lib_management.core.domain.repository.LanguageRepository
import sru.edu.sru_lib_management.core.domain.service.DonationService
import java.time.LocalDate

@Component
class DonationServiceImp(
    private val donationRepository: DonationRepository,
    private val bookRepository: BookRepository,
    private val languageRepository: LanguageRepository,
    private val collegeRepository: CollegeRepository
) : DonationService {

    private val logger = LoggerFactory.getLogger(DonationServiceImp::class.java)

    override suspend fun newDonation(donationDetails: List<DonationDetail>): CoreResult<List<Donator>> {
        return try {
            val saved = donationDetails.map { donationDetail ->
                val existingBook = bookRepository.getById(donationDetail.bookId)
                if (existingBook != null)
                    return CoreResult.ClientError("Book ID: ${donationDetail.bookId} is already exist.")
                collegeRepository.findById(donationDetail.collegeId).awaitSingle()
                    ?: return CoreResult.ClientError("College ID not found.")
                languageRepository.findById(donationDetail.languageId).awaitSingle()
                    ?: return CoreResult.ClientError("Language ID not found.")
                val book = Books(
                    bookId = donationDetail.bookId,
                    bookTitle = donationDetail.bookTitle,
                    bookQuan = donationDetail.bookQuan,
                    languageId = donationDetail.languageId,
                    collegeId = donationDetail.collegeId,
                    genre = donationDetail.genre,
                    author = donationDetail.author,
                    publicationYear = donationDetail.publicationYear,
                    isActive = true,
                    inactiveDate = null,
                    receiveDate = null
                )
                val donator = Donator(
                    donatorId = null,
                    donatorName = donationDetail.donatorName
                )
                bookRepository.save(book)
                val sponsorRepo =  donationRepository.save(donator)
                val sponsorships = Donation(
                    bookId = donationDetail.bookId,
                    donatorId = sponsorRepo.donatorId!!,
                    donateDate = donationDetail.donateDate
                )
                logger.info("$sponsorships")
                donationRepository.newDonation(sponsorships)
                donator
            }
            if (saved.size != donationDetails.size)
                return CoreResult.ClientError("Some books could not be saved due to invalid data or duplicate IDs")
            CoreResult.Success(saved)
        }catch(e:Exception) {
            e.printStackTrace()
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun updateDonation(donationDetail: DonationDetail): CoreResult<Donator> {
        return try {
            val findBook = bookRepository.getById(donationDetail.bookId) ?: return CoreResult.ClientError("Not found book.")
            donationRepository.getById(donationDetail.donatorId!!) ?: return CoreResult.ClientError("Not found donator.")

            collegeRepository.findById(donationDetail.collegeId).awaitSingleOrNull()
                ?: return CoreResult.ClientError("College ID not found.")
            languageRepository.findById(donationDetail.languageId).awaitSingleOrNull()
                ?: return CoreResult.ClientError("Language ID not found.")

            val book = Books(
                bookId = donationDetail.bookId,
                bookTitle = donationDetail.bookTitle,
                bookQuan = donationDetail.bookQuan,
                languageId = donationDetail.languageId,
                collegeId = donationDetail.collegeId,
                genre = donationDetail.genre,
                author = donationDetail.author,
                publicationYear = donationDetail.publicationYear,
                isActive = findBook.isActive,
                inactiveDate = findBook.inactiveDate,
                receiveDate = findBook.receiveDate
            )
            val donator = Donator(
                donationDetail.donatorId,
                donationDetail.donatorName
            )
            bookRepository.update(book)
            val update = donationRepository.update(donator)
            CoreResult.Success(update)
        }catch (e: Exception){
            e.printStackTrace()
            CoreResult.Failure(e.message.toString())
        }
    }

    override suspend fun getAllDonator(): Flow<Donator> {
        return try {
            donationRepository.getAll()
        }catch (e: Exception){
            throw APIException(e.message.toString())
        }
    }

    override suspend fun getDonator(donatorId: Int): CoreResult<Donator?> {
        return try {
            val donator = donationRepository.getById(donatorId)
            if (donator == null)
                CoreResult.ClientError("")
            else CoreResult.Success(donator)
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }


    override suspend fun deleteDonator(donatorId: Int): CoreResult<Boolean> {
        return runCatching {
            donationRepository.getById(donatorId)
                ?: return CoreResult.ClientError("Not found with ID: $donatorId")
            donationRepository.delete(donatorId)
        }.fold(
            onSuccess = {
                CoreResult.Success(true)
            },
            onFailure = {
                CoreResult.Failure(it.message.toString())
            }
        )
    }

    override fun getDonationDetail(): Flow<DonationDetailDto> {
        return try {
            donationRepository.getDonationDetail()
        }catch (e: Exception){
            throw APIException(e.message)
        }
    }

    override suspend fun deleteDonation(
        donatorId: Int?,
        bookId: String?
    ): CoreResult<Boolean> {
        return try {
            if ((donatorId == null && bookId == null) || (donatorId != null && bookId != null)) {
                return CoreResult.ClientError("Please provide either donatorId or bookId, but not both.")
            }
            val result = when {
                donatorId != null -> {
                    donationRepository.getById(donatorId)
                        ?: return CoreResult.ClientError("Not found donation with ID: $donatorId")
                    donationRepository.deleteDonation(donatorId, null)
                }
                bookId != null -> {
                    bookRepository.getById(bookId)
                        ?: return CoreResult.ClientError("Not found donation with ID: $bookId")
                    donationRepository.deleteDonation(null, bookId)
                }
                else -> false
            }
            if (result) {
                CoreResult.Success(true)
            } else {
                CoreResult.ClientError("Deletion failed. Please check the provided ID.")
            }
        }catch (e: Exception){
            CoreResult.Failure(e.message.toString())
        }
    }
    //////////////////
    /// Donation analytic
    ///////
    override suspend fun analyticDonation(date: LocalDate, period: Int): CoreResult<Analyze> {
        return runCatching {
            val invalidInput = date > LocalDate.now() || period <= 0
            val rightInput = period == 1 || period == 7 || period == 30 || period == 365
            if (invalidInput || !rightInput)
                return CoreResult.ClientError("Invalid data input please check date and period again.")
            val data = donationRepository.countCurrentAndPreviousDonation(date, period)
            val currentValue = data.currentValue
            val previousValue = data.previousValue
            val percentage = if (previousValue == 0){
                if (currentValue == 0) 0f else 100f
            }else {
                ((currentValue - previousValue)).toFloat() / previousValue * 100
            }
            Analyze(
                currentValue = currentValue,
                percentage = String.format("%.2f", percentage).toFloat()
            )
        }.fold(
            onSuccess = { data ->
                CoreResult.Success(data)
            },
            onFailure = { e ->
                println(e.printStackTrace())
                CoreResult.Failure(e.message.toString())
            }
        )
    }
}