/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.data.repository

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.*
import org.springframework.stereotype.Component
import sru.edu.sru_lib_management.core.data.query.BookQuery.DELETE_DONATION
import sru.edu.sru_lib_management.core.data.query.BookQuery.DELETE_DONATOR
import sru.edu.sru_lib_management.core.data.query.BookQuery.GET_ALL_DONATOR
import sru.edu.sru_lib_management.core.data.query.BookQuery.GET_DONATOR
import sru.edu.sru_lib_management.core.data.query.BookQuery.SAVE_DONATION
import sru.edu.sru_lib_management.core.data.query.BookQuery.SAVE_DONATOR
import sru.edu.sru_lib_management.core.data.query.BookQuery.UPDATE_DONATOR
import sru.edu.sru_lib_management.core.domain.dto.CompareValue
import sru.edu.sru_lib_management.core.domain.dto.DonationDetailDto
import sru.edu.sru_lib_management.core.domain.model.Donation
import sru.edu.sru_lib_management.core.domain.model.Donator
import sru.edu.sru_lib_management.core.domain.repository.DonationRepository
import java.time.LocalDate

@Component
class DonationRepositoryImp(
    private val client: DatabaseClient
) : DonationRepository {

    private val logger = LoggerFactory.getLogger(DonationRepositoryImp::class.java)

    //// This method use to save new donation after donator already saved
    override suspend fun newDonation(donation: Donation): Donation {
        logger.info("Donation Repository: $donation")
        client.sql(SAVE_DONATION)
            .bindValues(paramMapSponsorship(donation))
            .await()
        return donation
    }

    /// Count current and previous donation to find percentage change
    override suspend fun countCurrentAndPreviousDonation(
        date: LocalDate, period: Int
    ): CompareValue {
        return client.sql("CALL CountSponsorByPeriod(:date, :period)")
            .bind("date", date)
            .bind("period", period)
            .map { row ->
                CompareValue(
                    currentValue = row.get("current_value", Int::class.java)!!,
                    previousValue = row.get("previous_value", Int::class.java)!!
                )
            }
            .one()
            .awaitSingle()
    }

    /// Get Donation detail from table Book, Donator and Donation
    override fun getDonationDetail(): Flow<DonationDetailDto> {
        return client.sql("CALL DonationDetail()")
            .map { row: Row, _ ->
                row.rowMappingDonationDetail()
            }
            .all()
            .asFlow()
    }


    override suspend fun save(entity: Donator): Donator {
        val result: MutableMap<String, Any>? = client.sql(SAVE_DONATOR)
            .filter{s: Statement, next: ExecuteFunction ->
                next.execute(s.returnGeneratedValues("sponsor_id"))
            }
            .bind("sponsorName", entity.donatorName)
            .fetch()
            .awaitSingleOrNull()
        if (result != null && result.contains("sponsor_id")){
            val sponsorId = result["sponsor_id"] as Long
            entity.donatorId = sponsorId.toInt()
        }
        logger.info(entity.donatorId.toString())
        return entity
    }

    override suspend fun update(entity: Donator): Donator {
        client.sql(UPDATE_DONATOR)
            .bindValues(paramsMapDonator(entity))
            .fetch()
            .awaitRowsUpdated()
        return entity
    }

    override suspend fun getById(id: Int): Donator? {
        return client.sql(GET_DONATOR)
            .bind("donatorId", id)
            .map { row: Row, _ ->
                row.rowMappingDonator()
            }
            .awaitOneOrNull()
    }

    override fun getAll(): Flow<Donator> {
        return client.sql(GET_ALL_DONATOR)
            .map { row: Row, _ ->
                row.rowMappingDonator()
            }
            .all()
            .asFlow()
    }

    override suspend fun delete(id: Int): Boolean {
        val rowEffect = client.sql(DELETE_DONATOR)
            .bind("sponsorId", id)
            .fetch()
            .awaitRowsUpdated()
        return rowEffect > 0
    }

    override suspend fun deleteDonation(donatorId: Int?, bookId: String?): Boolean {
        val rowEffected = client.sql(DELETE_DONATION)
            .bind("donatorId", donatorId)
            .bind("bookId", bookId)
            .fetch()
            .awaitRowsUpdated()
        return rowEffected > 0
    }

    private fun Row.rowMappingDonator(): Donator = Donator(
        this.get("donator_Id", Int::class.java),
        this.get("donator_name", String::class.java)!!
    )

    private fun paramsMapDonator(bookDonator: Donator): Map<String, Any?> = mapOf(
        "donatorId" to bookDonator.donatorId,
        "donatorName" to bookDonator.donatorName
    )
    private fun paramMapSponsorship(donation: Donation): Map<String, Any?> = mapOf(
        "bookId" to donation.bookId,
        "donatorId" to donation.donatorId,
        "donateDate" to donation.donateDate
    )

    private fun Row.rowMappingDonationDetail(): DonationDetailDto = DonationDetailDto(
        this.get("donatorId", Int::class.java)!!,
        this.get("donatorName", String::class.java)!!,
        this.get("bookId", String::class.java)!!,
        this.get("bookTitle", String::class.java)!!,
        this.get("bookQuan", Int::class.java)!!,
        this.get("languageName", String::class.java)!!,
        this.get("collegeName", String::class.java)!!,
        this.get("author", String::class.java),
        this.get("publication_year", Int::class.java),
        this.get("genre", String::class.java)!!,
        this.get("donateDate", LocalDate::class.java)!!
    )
}